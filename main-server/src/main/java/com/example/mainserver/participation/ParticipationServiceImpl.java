package com.example.mainserver.participation;

import com.example.mainserver.event.EventRepository;
import com.example.mainserver.event.model.Event;
import com.example.mainserver.exceptions.ParticipationNoFoundException;
import com.example.mainserver.exceptions.WrongPatchException;
import com.example.mainserver.participation.model.*;
import com.example.mainserver.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.example.mainserver.event.model.State.PUBLISHED;
import static com.example.mainserver.participation.model.ParticipationMapper.toParticipationRequestDtoList;
import static com.example.mainserver.participation.model.StatusRequest.*;

@Service
@AllArgsConstructor
public class ParticipationServiceImpl implements ParticipationService {

    private final ParticipationRepository participationRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;


    @Override
    public ParticipationDto createParticipationRequest(Long userId, Long eventId) {
        if (participationRepository.findByEventIdAndRequesterId(eventId, userId) != null) {
            throw new WrongPatchException("participation request already exist");
        }

        Participation participation = Participation
                .builder()
                .created(LocalDateTime.now())
                .event(eventRepository.findById(eventId)
                        .orElseThrow(() -> new RuntimeException("user with id = " + userId + " not found")))
                .requester(userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("user with id = " + userId + " not found")))
                .status(PENDING)
                .build();
        if (userId.equals(participation.getEvent().getInitiator().getId())) {
            throw new WrongPatchException("requester can`t be initiator of event");
        }
        if (!participation.getEvent().getState().equals(PUBLISHED)) {
            throw new WrongPatchException("event not published");
        }
        if (Boolean.FALSE.equals(participation.getEvent().getRequestModeration())) {
            participation.setStatus(CONFIRMED);
        } else participation.setStatus(PENDING);

        int limit = participationRepository.countParticipationByEventIdAndStatus(eventId, CONFIRMED);
        if (participation.getEvent().getParticipantLimit() <= limit) {
            throw new WrongPatchException("the limit of requests for participation has been exhausted");
        }


        return ParticipationMapper.toParticipationDto(participationRepository.save(participation));
    }

    @Override
    public ParticipationListDto confirmParticipationRequest(Long userId, Long eventId, ParticipationChangeStatus participationChangeStatus) {
        List<ParticipationDto> confirmedRequests = new ArrayList<>();
        List<ParticipationDto> rejectedRequests = new ArrayList<>();
        List<Participation> requestsForUpdate = participationRepository.findStoredUpdRequests(eventId, participationChangeStatus.getRequestIds());

        for (Participation participation : requestsForUpdate) {

            if (participation.getStatus().equals(REJECTED)) {
                rejectedRequests.add(ParticipationMapper.toParticipationDto(participation));
            }
            if (participation.getStatus().equals(CONFIRMED)) {
                throw new WrongPatchException("only participation request with status pending can be approval");
            }
            if (participation.getStatus().equals(PENDING)) {
                if (participation.getEvent().getParticipantLimit() <= participationRepository.countParticipationByEventIdAndStatus(eventId, CONFIRMED)) {
                    participation.setStatus(REJECTED);
                    participationRepository.save(participation);
                    rejectedRequests.add(ParticipationMapper.toParticipationDto(participation));
                    throw new WrongPatchException("лимит участников исчерпан");
                } else {
                    participation.setStatus(participationChangeStatus.getStatus());
                    if (participation.getStatus().equals(REJECTED)) {
                        rejectedRequests.add(ParticipationMapper.toParticipationDto(participation));

                    }
                    if (participation.getStatus().equals(CONFIRMED)) {
                        participation.getEvent().setParticipantLimit(participation.getEvent().getParticipantLimit() - 1);
                        participationRepository.save(participation);
                        confirmedRequests.add(ParticipationMapper.toParticipationDto(participation));
                    }
                }
            }
        }



        return ParticipationListDto
                .builder()
                .confirmedRequests(confirmedRequests)
                .rejectedRequests(rejectedRequests)
                .build();
    }


    @Override
    public ParticipationDto cancelParticipationRequest(Long userId, Long reqId) {
        Participation participation = participationRepository.canselParticipationRequest(reqId, userId);
        if (participation == null)
            throw new ParticipationNoFoundException("Request with id=" + reqId + " was not found");
        participation.setStatus(CANCELED);
        return ParticipationMapper.toParticipationDto(participationRepository.save(participation));
    }

    @Override
    public List<ParticipationDto> getParticipationRequests(Long userId) {
        List<Participation> list = participationRepository.findAllByRequesterId(userId);
        List<ParticipationDto> listDto = new ArrayList<>();
        for (Participation participation : list) {
            listDto.add(ParticipationMapper.toParticipationDto(participation));
        }
        return listDto;
    }


    @Override
    public List<ParticipationRequestDto> getRequestsByEventIdAndInitiatorId(Long eventId, Long userId) {
        getByIdAndInitiatorIdWithCheck(eventId, userId);
        return toParticipationRequestDtoList(participationRepository.findAllByEventId(eventId));
    }

    private Event getByIdAndInitiatorIdWithCheck(Long eventId, Long initiatorId) {
        return eventRepository.findByIdAndInitiatorId(eventId, initiatorId)
                .orElseThrow(() -> new RuntimeException(String.format("Event with id=%d and initiatorId=%d was not found", eventId, initiatorId)));
    }
}

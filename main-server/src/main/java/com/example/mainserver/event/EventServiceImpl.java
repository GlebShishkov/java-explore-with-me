package com.example.mainserver.event;

import com.example.mainserver.category.model.Category;
import com.example.mainserver.category.repository.CategoryRepository;
import com.example.mainserver.event.model.*;
import com.example.mainserver.exceptions.EventNotFoundException;

import com.example.mainserver.exceptions.EventPublishedException;
import com.example.mainserver.exceptions.WrongDateException;
import com.example.mainserver.location.model.Location;
import com.example.mainserver.location.LocationRepository;
import com.example.mainserver.user.UserRepository;
import com.example.mainserver.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.example.mainserver.event.model.State.PUBLISHED;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {


    private final EventRepository eventRepository;
    //  private final ParticipationRepository participationRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;


    @Override
    public EventDto createEvent(Long userId, NewEventDto eventDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("user with id = " + userId + " not found"));

        Event event = EventMapper.toEvent(eventDto);
        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            throw new WrongDateException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value:" + (event.getEventDate()));
        }
        Location location = locationRepository.save(eventDto.getLocation());

        event.setCategory(categoryRepository.findById(eventDto.getCategory())
                .orElseThrow(() -> new EventNotFoundException("Field: category. Error: must not be blank. Value: null")));


        event.setLocation(location);
        event.setInitiator(user);
        return EventMapper.toEventDto(eventRepository.save(event));
    }

    @Override
    public EventDto publishEvent(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("event with id = " + eventId + " not found"));
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new RuntimeException("event must start min after one hour of now");
        }

        if (updateEventAdminRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventAdminRequest.getCategory())
                    .orElseThrow(() -> new RuntimeException("category with id = " + updateEventAdminRequest.getCategory() + " not found"));
            event.setCategory(category);
        }
        if (updateEventAdminRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventAdminRequest.getAnnotation());
        }

        if (updateEventAdminRequest.getDescription() != null) {
            event.setDescription(updateEventAdminRequest.getDescription());
        }
        if (updateEventAdminRequest.getEventDate() != null) {
            LocalDateTime eventDate = LocalDateTime.parse(updateEventAdminRequest.getEventDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            event.setEventDate(eventDate);
        }
        if (updateEventAdminRequest.getLocation() != null) {
            Location location = locationRepository.findById(event.getLocation().getId())
                    .orElseThrow(() -> new RuntimeException("user with id = " + event.getLocation().getId() + " not found"));
            location.setLat(updateEventAdminRequest.getLocation().getLat());
            location.setLon(updateEventAdminRequest.getLocation().getLon());
            event.setLocation(location);
        }
        if (updateEventAdminRequest.getPaid() != null) {
            event.setPaid(updateEventAdminRequest.getPaid());
        }
        if (updateEventAdminRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventAdminRequest.getParticipantLimit());
        }
        if (updateEventAdminRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventAdminRequest.getRequestModeration());
        }
        if (updateEventAdminRequest.getTitle() != null)
            event.setTitle(updateEventAdminRequest.getTitle());

        event.setState(PUBLISHED);
        return EventMapper.toEventDto(eventRepository.save(event));

    }

    @Override
    public EventDto getEvent(Long userId, Long eventId) {

        Event event = eventRepository.getEventByUser(userId, eventId);
        if (event == null)
            throw new EventNotFoundException("Event with id=" + eventId + " was not found");
        Long views = event.getViews();
        event.setViews(views + 1);
        eventRepository.save(event);
        return EventMapper.toEventDto(event);
    }

    @Override
    public List<EventDtoShort> getEventByUser(Long userId, int from, int size) {


        Pageable pageable = PageRequest.of(from, size, Sort.by(Sort.Direction.ASC, "id"));
        List<Event> events = eventRepository.getEventsByUser(userId, pageable);
        List<EventDtoShort> eventDtos = new ArrayList<>();
        for (Event event : events) {
            eventDtos.add(EventMapper.toEventDtoShort(event));
        }

        return eventDtos;
    }

    @Override
    public EventDto putchEvent(Long userId, Long eventId, EventDtoShort eventDtoShort) {
        Event event = eventRepository.getEventByUser(userId, eventId);

        if (event.getState() == PUBLISHED)
            throw new EventPublishedException("Event must  be published");
        if (event == null)
            throw new EventNotFoundException("Event with id = " + eventId + " was not found");
        if (eventDtoShort.getAnnotation() != null) {
            event.setAnnotation(eventDtoShort.getAnnotation());
        }

        if (eventDtoShort.getDescripsion() != null) {
            event.setDescription(eventDtoShort.getDescripsion());
        }

        if (eventDtoShort.getEventDate() != null) {
            LocalDateTime startEvent = LocalDateTime.parse(eventDtoShort.getEventDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            event.setEventDate(startEvent);
        }
        if (eventDtoShort.getLocation() != null)
            event.setLocation(eventDtoShort.getLocation());

        if (eventDtoShort.getPaid() != null) {
            event.setPaid(eventDtoShort.getPaid());
        }

        if (eventDtoShort.getParticipantLimit() != 0) {
            event.setParticipantLimit(eventDtoShort.getParticipantLimit());
        }

        if (eventDtoShort.getTitle() != null) {
            event.setTitle(eventDtoShort.getTitle());
        }
        return EventMapper.toEventDto(event);
    }


}



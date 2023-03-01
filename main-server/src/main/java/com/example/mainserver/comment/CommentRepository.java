package com.example.mainserver.comment;

import com.example.mainserver.comment.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {


    @Query("select c from Comment c where  c.event.id =?1 " +
            "order by c.createdOn asc")
    List<Comment> findAllByEventId(Long eventId);
}

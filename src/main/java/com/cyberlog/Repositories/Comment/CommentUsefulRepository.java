package com.cyberlog.Repositories.Comment;

import com.cyberlog.Models.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentUsefulRepository extends JpaRepository<CommentUseful, UUID> {
    Optional<CommentUseful> findByUserAndComment(User user, Comment comment);

    Iterable<? extends CommentUseful> findByComment(Comment comment);

    void deleteByCommentIn(List<Comment> comments);
}

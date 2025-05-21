package com.cyberlog.Repositories.Comment;

import com.cyberlog.Models.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentLikeRepository extends JpaRepository<CommentLike, UUID> {
    Optional<CommentLike> findByUserAndComment(User user, Comment comment);

    Iterable<? extends CommentLike> findByComment(Comment comment);

    void deleteByCommentIn(List<Comment> comments);

    void deleteByUser(User user);
}

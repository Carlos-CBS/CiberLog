package com.cyberlog.Repositories.Comment;

import com.cyberlog.Models.Article;
import com.cyberlog.Models.Comment;
import com.cyberlog.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByArticleOrderByCreatedAtAsc(Article article);

    List<Comment> findByArticle(Article article);

    void deleteByUser(User user);
}
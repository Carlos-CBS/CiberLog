package com.cyberlog.Repositories.Article;

import com.cyberlog.Models.Article;
import com.cyberlog.Models.ArticleLike;
import com.cyberlog.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ArticleLikeRepository extends JpaRepository<ArticleLike, UUID> {
    Optional<ArticleLike> findByUserAndArticle(User user, Article article);
    Long countByArticle(Article article);

    Iterable<? extends ArticleLike> findByArticle(Article article);
}

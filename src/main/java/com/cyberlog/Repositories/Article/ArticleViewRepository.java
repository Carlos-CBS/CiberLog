package com.cyberlog.Repositories.Article;

import com.cyberlog.Models.Article;
import com.cyberlog.Models.ArticleView;
import com.cyberlog.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface ArticleViewRepository extends JpaRepository<ArticleView, UUID> {
    Optional<ArticleView> findTopByUserAndArticleOrderByViewedAtDesc(User user, Article article);
    long countByArticle(Article article);
    Optional<ArticleView> findTopByVisitorIdAndArticleOrderByViewedAtDesc(String visitorId, Article article);

    void deleteByArticleId(UUID id);

    void deleteAllByArticle(Article article);

    void deleteByUser(User user);
}

package com.cyberlog.Repositories.Article;

import com.cyberlog.Models.Article;
import com.cyberlog.Models.ArticleUseful;
import com.cyberlog.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ArticleUsefulRepository extends JpaRepository<ArticleUseful, UUID> {
    Optional<ArticleUseful> findByUserAndArticle(User user, Article article);
    Long countByArticle(Article article);

    Iterable<? extends ArticleUseful> findByArticle(Article article);
}

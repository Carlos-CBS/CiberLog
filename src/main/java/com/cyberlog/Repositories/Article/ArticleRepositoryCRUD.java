package com.cyberlog.Repositories.Article;

import com.cyberlog.Models.Article;
import com.cyberlog.Models.Collection;
import com.cyberlog.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArticleRepositoryCRUD extends JpaRepository<Article, UUID> {
    List<Article> findByUser(User user);
    List<Article> findByCollection(Collection collection);
    boolean existsByCollectionAndSlug(Collection collection, String slug);
    Optional<Article> findBySlug(String slug);

    Optional<Article> findByUserAndSlug(User user, String articleSlug);

    void deleteAllByCollection(Collection blog);


    List<Article> findByStatus(Article.Status status);

    List<Article> findByStatusAndTags_IdIn(Article.Status status, List<UUID> tagIds);

    void deleteByUser(User user);
}
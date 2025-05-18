package com.cyberlog.Repositories;

import com.cyberlog.Models.Article;
import com.cyberlog.Models.Collection;
import com.cyberlog.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, UUID> {
    List<Collection> findByUser(User user);
    boolean existsBySlug(String slug);

    Optional<Collection> findById(UUID id);
    Optional<Object> findBySlug(String collectionSlug);

}
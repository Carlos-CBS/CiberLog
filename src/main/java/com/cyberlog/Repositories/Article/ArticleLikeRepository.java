package com.cyberlog.Repositories.Article;

import com.cyberlog.Models.ArticleLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ArticleLikeRepository extends JpaRepository<ArticleLike, UUID> {
}

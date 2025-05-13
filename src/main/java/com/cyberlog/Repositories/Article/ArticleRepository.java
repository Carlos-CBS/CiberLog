package com.cyberlog.Repositories.Article;

import com.cyberlog.Models.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ArticleRepository extends JpaRepository<Article, UUID> {
}

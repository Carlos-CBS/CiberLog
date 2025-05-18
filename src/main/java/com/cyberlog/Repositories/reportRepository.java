package com.cyberlog.Repositories;

import com.cyberlog.Models.Article;
import com.cyberlog.Models.Comment;
import com.cyberlog.Models.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface reportRepository extends JpaRepository<Report, UUID> {
    List<Report> findAllByOrderByCreatedAtDesc();

    List<Report> findByComment(Comment comment);

    List<Report> findByArticle(Article article);
}

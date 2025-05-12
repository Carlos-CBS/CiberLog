package com.cyberlog.Models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "comment") // Explicitly specify table name
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(nullable = false, name = "article_id")
    private Article article;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime created_at = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updated_at = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updated_at = LocalDateTime.now();
    }

    @Column(name = "comment_like", nullable = false)
    private int commentLike;

    @Column(name = "comment_useful", nullable = false)
    private int commentUseful;

    @ManyToOne
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;
}

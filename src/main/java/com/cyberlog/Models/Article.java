package com.cyberlog.Models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"collection_id", "slug"})
})
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "collection_id")
    private Collection collection;

    @ManyToOne
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @Column(nullable = true)
    private String slug;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String summary;

    @Column(nullable = false)
    private int reading_time_minutes;

    @Column(nullable = false)
    private LocalDateTime created_at = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updated_at = LocalDateTime.now();

    @PreUpdate
    public void onUpdate() {
        this.updated_at = LocalDateTime.now();
    }

    @Column
    private LocalDateTime published_at;

    @Column(nullable = false)
    private int views = 0;

    public enum Status {
        draft, review, published
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.draft;

    @Column(nullable = false)
    private int comment_count = 0;

    @Column(nullable = false)
    private int likes_count = 0;

    @Column(nullable = false)
    private int useful_count = 0;

    public enum Difficulty {
        basic, intermediate, advanced
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty = Difficulty.basic;

}

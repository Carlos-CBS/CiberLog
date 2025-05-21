package com.cyberlog.Models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
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

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(nullable = false)
    private String summary;

    @Column(nullable = true)
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

    @Formula("(select count(*) from article_view al where al.article_id = id)")
    private int views = 0;

    public enum Status {
        draft, published
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.draft;

    @Formula("(select count(*) from comment al where al.article_id = id)")
    private int comment_count = 0;

    @Formula("(select count(*) from article_like al where al.article_id = id)")
    private long likesCount;

    @Formula("(select count(*) from article_useful al where al.article_id = id)")
    private int useful_count = 0;

    public enum Difficulty {
        basic, intermediate, advanced
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty = Difficulty.basic;

    @ManyToMany
    @JoinTable(
            name = "article_tag",
            joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"article_id", "tag_id"})
    )
    private Set<Tag> tags = new HashSet<>();


}

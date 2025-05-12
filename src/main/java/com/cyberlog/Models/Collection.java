package com.cyberlog.Models;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Collection {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String slug;

    @Column(nullable = true)
    private String summary;

    @Column(nullable = false)
    private String image_url;

    @Column(nullable = false)
    private LocalDateTime created_at = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updated_at = LocalDateTime.now();

    @PreUpdate
    public void onUpdate() {
        this.updated_at = LocalDateTime.now();
    }

    public enum Status {
        draft, review, published
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.draft;



}

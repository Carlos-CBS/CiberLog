package com.cyberlog.Models;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleView {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    private Article article;

    @ManyToOne
    private User user;

    private String visitorId;

    private LocalDateTime viewedAt;
}


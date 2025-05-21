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
public class DeletedRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String recordType;

    @Column(nullable = false)
    private UUID originalId;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String authorName;

    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime deletedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private Report.Status status;

}

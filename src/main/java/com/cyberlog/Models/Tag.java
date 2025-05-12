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
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column
    private LocalDateTime created_at = LocalDateTime.now();

}

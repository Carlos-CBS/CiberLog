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

    // Tipo de contenido eliminado (puede ser "comment" o "article")
    @Column(nullable = false)
    private String recordType;

    // ID original del comentario o artículo
    @Column(nullable = false)
    private UUID originalId;

    // Contenido eliminado (texto del comentario o artículo)
    @Column(columnDefinition = "TEXT")
    private String content;

    // Usuario que creó el contenido
    private String authorName;

    // Fecha de creación del contenido
    private LocalDateTime createdAt;

    // Fecha en la que se eliminó
    @Column(nullable = false)
    private LocalDateTime deletedAt = LocalDateTime.now();

    // Puedes añadir más campos según necesidades
}

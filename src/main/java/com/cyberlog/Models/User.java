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
@Table(name = "`user`")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String password;

    public enum Role {
        admin, editor, reader
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.editor;

    @Column
    private String avatar;

    @Column(columnDefinition = "TEXT")
    private String bio;


    // usare un textArea
    @Column
    private String socialLinks;

    @Column(nullable = false)
    private LocalDateTime created_at = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updated_at = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updated_at = LocalDateTime.now();
    }


    // se configura en el servicio de autenticación
    @Column(nullable = false)
    private LocalDateTime last_login;

}

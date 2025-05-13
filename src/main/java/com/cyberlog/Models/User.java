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
        admin, user, moderator
    }


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.user;

    @Column
    private String avatar;

    @Column(columnDefinition = "TEXT")
    private String bio;

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

    @Column(nullable = false)
    private LocalDateTime last_login;

    @PrePersist
    public void prePersist() {
        if (this.last_login == null) {
            this.last_login = LocalDateTime.now();
        }
    }
}

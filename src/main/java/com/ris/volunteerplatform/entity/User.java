package com.ris.volunteerplatform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Builder.Default
    private UUID uuid = UUID.randomUUID();

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UserRole role;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "profile_full_name")
    private String profileFullName;

    @Column(name = "profile_phone", length = 50)
    private String profilePhone;

    @Column(name = "profile_bio", columnDefinition = "TEXT")
    private String profileBio;

    @Column(name = "profile_avatar_url", columnDefinition = "TEXT")
    private String profileAvatarUrl;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (uuid == null) uuid = UUID.randomUUID();
    }
}

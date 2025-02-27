package com.vinova.booking_hotel.authentication.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "account")
public class Account extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "block_reason", columnDefinition = "TEXT")
    private String blockReason = null;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "refresh_expires_at")
    private LocalDateTime refreshExpiresAt;
    
}

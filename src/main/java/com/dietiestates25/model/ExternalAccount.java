package com.dietiestates25.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "external_account", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "provider", "provider_user_id" })
})
public class ExternalAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String provider; // e.g., 'google', 'facebook'

    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}

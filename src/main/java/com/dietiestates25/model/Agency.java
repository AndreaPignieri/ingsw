package com.dietiestates25.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "agency")
public class Agency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 150)
    private String email;

    @Column(length = 50)
    private String phone;

    @Column(length = 255)
    private String address;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}

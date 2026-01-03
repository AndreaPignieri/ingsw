package com.dietiestates25.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "amenity")
public class Amenity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;
}

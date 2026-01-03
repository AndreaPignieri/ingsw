package com.dietiestates25.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "property")
public class Property {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PropertyType type;

    @Column(name = "size_sqm")
    private Integer sizeSqm;

    private Integer rooms;
    private Integer floor;

    @Enumerated(EnumType.STRING)
    @Column(name = "energy_class", length = 5)
    private EnergyClass energyClass;

    @Column(length = 255)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "published_at", insertable = false, updatable = false)
    private LocalDateTime publishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private Agent agent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id", nullable = false)
    private Agency agency;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PropertyPhoto> photos = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "property_amenity", joinColumns = @JoinColumn(name = "property_id"), inverseJoinColumns = @JoinColumn(name = "amenity_id"))
    private Set<Amenity> amenities = new HashSet<>();
}

package com.dietiestates25.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "agent")
@PrimaryKeyJoinColumn(name = "id")
public class Agent extends User {

    @Column(columnDefinition = "TEXT")
    private String biography;

    @Column(name = "profile_photo", length = 255)
    private String profilePhoto;
}

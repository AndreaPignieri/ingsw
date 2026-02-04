package com.dietiestates25.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "agent")
@PrimaryKeyJoinColumn(name = "id")
public class Agent extends User {

    @Column(columnDefinition = "TEXT")
    private String biography;

    @Column(name = "profile_photo", length = 255)
    private String profilePhoto;

    @Column(name = "birth_date")
    private java.time.LocalDate birthDate;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
}

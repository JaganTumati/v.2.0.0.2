package com.restaurant.ai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "restaurants")
@Getter
@Setter
@NoArgsConstructor
public class Restaurant {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "cuisine_type", length = 120)
    private String cuisineType;

    @Column(length = 30)
    private String phone;

    @Column(nullable = false, length = 10)
    private String currency = "INR";

    /** OPEN, CLOSED, PAUSED */
    @Column(nullable = false, length = 20)
    private String status = "OPEN";

    @Column(name = "address_line", length = 300)
    private String addressLine;

    @Column(length = 120)
    private String city;

    @Column(length = 120)
    private String state;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(length = 120)
    private String country = "India";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

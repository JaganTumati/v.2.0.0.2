package com.restaurant.ai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "menu_item_id", nullable = false, unique = true)
    private UUID menuItemId;

    @Column(name = "track_stock", nullable = false)
    private boolean trackStock = false;

    @Column(name = "stock_count")
    private Integer stockCount;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

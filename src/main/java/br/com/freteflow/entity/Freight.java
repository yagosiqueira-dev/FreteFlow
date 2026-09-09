package br.com.freteflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "freights")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Freight {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "tb_freight_store",
            joinColumns = @JoinColumn(name = "freight_id"),
            inverseJoinColumns = @JoinColumn(name = "store_id")
    )
    private List<Store> stores= new ArrayList<>();

    @Column(name = "freight_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal freightValue;

    @Column(name = "freight_date", nullable = false)
    private LocalDateTime freightDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FreightStatus status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = FreightStatus.DELIVERED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
package com.progra3.cafeteria_api.model.entity;

import com.progra3.cafeteria_api.model.enums.SeatingShape;
import com.progra3.cafeteria_api.model.enums.SeatingSize;
import com.progra3.cafeteria_api.model.enums.SeatingStatus;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "seating")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Seating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer number;

    @Column(nullable = false)
    private Integer posX;

    @Column(nullable = false)
    private Integer posY;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatingShape shape;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatingSize size;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "active_order_id")
    private Order activeOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatingStatus status;

    @Column(nullable = false)
    private Boolean deleted = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;
}
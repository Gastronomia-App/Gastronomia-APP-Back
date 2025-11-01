package com.progra3.cafeteria_api.model.entity;

import com.progra3.cafeteria_api.model.enums.SeatingOrientation;
import com.progra3.cafeteria_api.model.enums.SeatingShape;
import com.progra3.cafeteria_api.model.enums.SeatingSize;
import com.progra3.cafeteria_api.model.enums.SeatingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;


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

    @Column(nullable = false)
    private Integer width = 1;
    @Column(nullable = false)
    private Integer height = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatingShape shape;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatingSize size;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatingOrientation orientation;

    @OneToMany(mappedBy = "seating", cascade = CascadeType.ALL)
    private List<Order> orders;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatingStatus status;

    @Column(nullable = false)
    private Boolean deleted = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;
}
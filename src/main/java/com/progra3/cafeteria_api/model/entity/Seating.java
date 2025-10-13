package com.progra3.cafeteria_api.model.entity;

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

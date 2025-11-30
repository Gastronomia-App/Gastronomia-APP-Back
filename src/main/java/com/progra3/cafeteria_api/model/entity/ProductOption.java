package com.progra3.cafeteria_api.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_group_options")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductOption {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_group_id")
    private ProductGroup productGroup;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "max_quantity")
    private Integer maxQuantity;

    @Column(name = "price_increase")
    private Double priceIncrease;
}
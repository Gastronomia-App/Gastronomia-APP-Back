package com.progra3.cafeteria_api.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_groups", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "business_id"})
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "min_quantity")
    private Integer minQuantity;

    @Column(name = "max_quantity")
    private Integer maxQuantity;

    @OneToMany(mappedBy = "productGroup",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductOption> options = new ArrayList<>();

    @ManyToMany(mappedBy = "productGroups")
    private List<Product> usedByProducts = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;
}


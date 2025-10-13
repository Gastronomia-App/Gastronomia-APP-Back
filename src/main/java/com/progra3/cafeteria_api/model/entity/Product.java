package com.progra3.cafeteria_api.model.entity;

import com.progra3.cafeteria_api.model.enums.CompositionType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "products", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "business_id"})
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column
    private String description;

    @Column(nullable = false)
    private Double price;

    @Column
    private Double cost;

    @Column
    private Integer stock;

    @Column(nullable = false)
    private Boolean deleted;

    @OneToMany(mappedBy = "parentProduct", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductComponent> components = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "groups_by_products",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "product_group_id")
    )
    private Set<ProductGroup> productGroups = new HashSet<>();

    @Column(nullable = false)
    private boolean composite;

    @Column(name= "composition_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private CompositionType compositionType;

    @Column(name = "control_stock", nullable = false)
    private boolean controlStock;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;
}
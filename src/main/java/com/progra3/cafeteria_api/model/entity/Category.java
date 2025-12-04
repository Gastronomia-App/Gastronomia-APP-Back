package com.progra3.cafeteria_api.model.entity;

import com.progra3.cafeteria_api.model.enums.CategoryIcon;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "business_id"})
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String color;

    @Enumerated(EnumType.STRING)
    @Column(name = "icon")
    private CategoryIcon icon;

    @Column(name = "visible_in_menu")
    private Boolean visibleInMenu;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Product> products = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;
}

package com.progra3.cafeteria_api.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "selected_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelectedOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Only Level 1 options have an item reference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_option_id")
    private SelectedOption parentOption;

    @OneToMany(mappedBy = "parentOption", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SelectedOption> selectedOptions = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_option_id", nullable = false)
    private ProductOption productOption;

    @Column(nullable = false)
    private Integer quantity;

    /**
     * Adds a nested option to this option.
     * Nested options (level 2+) should NOT have the item reference set.
     * Only the parentOption is set to maintain the tree structure.
     */
    public void addSelectedOption(SelectedOption option) {
        selectedOptions.add(option);
        option.setParentOption(this);
        // DO NOT set item here - only level 1 options have item reference
    }

    /**
     * Helper method to get the root item by traversing up the tree.
     * Level 1 options have item directly, nested options must traverse up.
     */
    public Item getRootItem() {
        if (this.item != null) {
            return this.item;
        }
        if (this.parentOption != null) {
            return this.parentOption.getRootItem();
        }
        return null;
    }
}
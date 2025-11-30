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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
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

    public void addSelectedOption(SelectedOption option) {
        selectedOptions.add(option);
        option.setParentOption(this);
        option.setItem(this.item);
    }
}
package com.progra3.cafeteria_api.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "suppliers",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"legal_name", "business_id"}),
                @UniqueConstraint(columnNames = {"cuit", "business_id"}),
                @UniqueConstraint(columnNames = {"email", "business_id"}),
                @UniqueConstraint(columnNames = {"phone_number", "business_id"})
        })
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Supplier {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @Column(name = "legal_name")
    private String legalName;

    @Column(name = "trade_name", nullable = false)
    private String tradeName;

    @Column(name = "cuit")
    private String cuit;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @Embedded
    private Address address;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;
}

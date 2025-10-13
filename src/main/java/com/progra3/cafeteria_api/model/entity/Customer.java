package com.progra3.cafeteria_api.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;
import lombok.experimental.SuperBuilder;


@Entity
@Table(name = "customers", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"dni", "business_id"}),
        @UniqueConstraint(columnNames = {"email", "business_id"}),
        @UniqueConstraint(columnNames = {"phone_number", "business_id"})
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class Customer extends Person {


    @Column
    private Integer discount;
}
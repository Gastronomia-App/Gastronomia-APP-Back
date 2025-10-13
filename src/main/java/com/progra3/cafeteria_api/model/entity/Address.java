package com.progra3.cafeteria_api.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Address {
    @Column
    private String street;

    @Column
    private String city;

    @Column
    private String province;

    @Column
    private String zipCode;
}

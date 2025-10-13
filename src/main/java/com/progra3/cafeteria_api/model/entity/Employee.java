package com.progra3.cafeteria_api.model.entity;

import com.progra3.cafeteria_api.model.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "employees", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"dni", "business_id"}),
        @UniqueConstraint(columnNames = {"email", "business_id"}),
        @UniqueConstraint(columnNames = {"phone_number", "business_id"})
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class Employee extends Person{

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
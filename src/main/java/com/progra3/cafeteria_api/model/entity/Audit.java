package com.progra3.cafeteria_api.model.entity;

import com.progra3.cafeteria_api.model.enums.AuditStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "audits")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Audit{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "close_time")
    private LocalDateTime closeTime;

    @Column(name = "initial_cash", nullable = false)
    private Double initialCash;

    @Column(name = "real_cash", nullable = false)
    private Double realCash;

    @OneToMany(mappedBy = "audit", cascade = CascadeType.ALL)
    private List<Order> orders;

    @OneToMany(mappedBy = "audit", cascade = CascadeType.ALL)
    private List<Expense> expenses;

    @Enumerated(EnumType.STRING)
    @Column(name = "audit_status", nullable = false)
    private AuditStatus auditStatus;

    @Column(name = "total_expensed")
    private Double totalExpensed;

    @Column
    private Double total;

    @Column(name = "balance_gap")
    private Double balanceGap;

    @Column(nullable = false)
    private Boolean deleted;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;
}
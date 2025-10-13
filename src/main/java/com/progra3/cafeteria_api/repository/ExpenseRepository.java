package com.progra3.cafeteria_api.repository;

import com.progra3.cafeteria_api.model.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    Optional<Expense> findByIdAndBusiness_Id(Long id, Long businessId);

    @Query("SELECT e FROM Expense e WHERE " +
            "e.business.id = :businessId AND " +
            "e.deleted = false AND" +
            "(:supplierId IS NULL OR e.supplier.id = :supplierId) AND " +
            "(:minAmount IS NULL OR e.amount >= :minAmount) AND " +
            "(:maxAmount IS NULL OR e.amount <= :maxAmount) AND " +
            "(:startDate IS NULL OR e.dateTime >= :startDate) AND " +
            "(:endDate IS NULL OR e.dateTime <= :endDate) ")
    Page<Expense> findByBusiness_Id(@Param("supplierId") Long supplierId,
                                    @Param("minAmount") Double minAmount,
                                    @Param("maxAmount") Double maxAmount,
                                    @Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate,
                                    @Param("businessId") Long businessId,
                                    Pageable pageable);

    List<Expense> findByDateTimeBetweenAndBusiness_Id(LocalDateTime start, LocalDateTime end, Long businessId);

    boolean existsByIdAndBusiness_Id(Long id, Long businessId);
}
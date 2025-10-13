package com.progra3.cafeteria_api.repository;

import com.progra3.cafeteria_api.model.entity.Audit;
import com.progra3.cafeteria_api.model.enums.AuditStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AuditRepository extends JpaRepository<Audit, Long> {
    Optional<Audit> findByBusiness_IdAndAuditStatus(Long business_id, AuditStatus status);

    @Query("SELECT a FROM Audit a WHERE " +
            "a.business.id = :businessId AND " +
            "(:startDate IS NULL OR a.closeTime >= :startDate) AND " +
            "(:endDate IS NULL OR a.closeTime <= :endDate) ")
    Page<Audit> findByBusiness_Id(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate,
                                    @Param("businessId") Long businessId,
                                    Pageable pageable);

    Optional<Audit> findByIdAndBusiness_Id(Long id, Long business_id);

    boolean existsByBusiness_Id(Long business_id);
}
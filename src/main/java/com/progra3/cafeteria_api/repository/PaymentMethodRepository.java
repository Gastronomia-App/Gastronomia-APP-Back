package com.progra3.cafeteria_api.repository;

import com.progra3.cafeteria_api.model.entity.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    Optional<PaymentMethod> findByIdAndBusiness_Id(Long id, Long businessId);

    Optional<PaymentMethod> findByNameAndBusiness_Id(String name, Long businessId);

    boolean existsByNameAndBusiness_Id(String name, Long businessId);

    @Query("SELECT pm FROM PaymentMethod pm WHERE " +
            "pm.business.id = :businessId AND " +
            "pm.deleted = false AND " +
            "(:name IS NULL OR LOWER(pm.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<PaymentMethod> findByBusiness_Id(@Param("name") String name,
                                          @Param("businessId") Long businessId,
                                          Pageable pageable);
}


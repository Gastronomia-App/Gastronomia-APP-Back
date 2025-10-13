package com.progra3.cafeteria_api.repository;

import com.progra3.cafeteria_api.model.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findByCuitAndBusiness_Id(String cuit, Long businessId);
    Optional<Supplier> findByIdAndBusiness_Id(Long id, Long businessId);

    @Query("SELECT s FROM Supplier s WHERE " +
            "s.business.id = :businessId AND " +
            "s.deleted = false AND " +
            "(:tradeName IS NULL OR LOWER(s.tradeName) LIKE LOWER(CONCAT('%', :tradeName, '%'))) AND " +
            "(:legalName IS NULL OR LOWER(s.legalName) LIKE LOWER(CONCAT('%', :legalName, '%'))) AND " +
            "(:cuit IS NULL OR s.cuit LIKE CONCAT('%', :cuit, '%'))")
    Page<Supplier> findByBusiness_Id(@Param("tradeName") String tradeName,
                                     @Param("legalName") String legalName,
                                     @Param("cuit") String cuit,
                                     Long businessId,
                                     Pageable pageable);
}
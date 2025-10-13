package com.progra3.cafeteria_api.repository;

import com.progra3.cafeteria_api.model.entity.ProductGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductGroupRepository extends JpaRepository<ProductGroup, Long> {
    Optional<ProductGroup> findByIdAndBusiness_Id(Long id, Long businessId);

    List<ProductGroup> findByBusiness_Id(Long businessId);
}

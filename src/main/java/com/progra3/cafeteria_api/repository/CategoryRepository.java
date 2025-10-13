package com.progra3.cafeteria_api.repository;

import com.progra3.cafeteria_api.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByBusiness_Id(Long businessId);
    Optional<Category> findByIdAndBusiness_Id(Long id, Long businessId);
}

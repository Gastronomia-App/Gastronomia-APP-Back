package com.progra3.cafeteria_api.repository;

import com.progra3.cafeteria_api.model.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {
    Optional<Business> findByIdAndDeletedFalse(Long id);
    boolean existsByName(String name);
    boolean existsByCuit(String cuit);
    boolean existsBySlug(String slug);
    boolean existsByNameAndIdNot(String name, Long id);
    boolean existsByCuitAndIdNot(Long cuit, Long id);
    Optional<Business> findBySlugAndDeletedFalse(String slug);
}

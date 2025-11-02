package com.progra3.cafeteria_api.repository;

import com.progra3.cafeteria_api.model.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {
    Optional<Business> findByIdAndDeletedFalse(Long id);
}

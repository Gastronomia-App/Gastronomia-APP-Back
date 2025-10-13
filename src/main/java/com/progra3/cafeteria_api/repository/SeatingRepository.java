package com.progra3.cafeteria_api.repository;

import com.progra3.cafeteria_api.model.entity.Seating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatingRepository extends JpaRepository<Seating, Long> {
    Optional<Seating> findByNumberAndBusiness_Id(Integer number, Long businessId);

    Optional<Seating> findByIdAndBusiness_Id(Long id, Long businessId);

    List<Seating> findByBusiness_Id(Long businessId);
}

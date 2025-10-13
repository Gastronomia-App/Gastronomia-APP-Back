package com.progra3.cafeteria_api.repository;

import com.progra3.cafeteria_api.model.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByDniAndBusiness_Id(String dni, Long businessId);

    @Query("SELECT c FROM Customer c WHERE " +
            "c.business.id = :businessId AND " +
            "c.deleted = false AND" +
            "(:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:lastName IS NULL OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) AND " +
            "(:dni IS NULL OR c.dni LIKE CONCAT('%', :dni, '%')) AND " +
            "(:email IS NULL OR LOWER(c.email) LIKE LOWER(CONCAT('%', :email, '%'))) ")
    Page<Customer> findByBusiness_Id(@Param("name") String name,
                                     @Param("lastName") String lastName,
                                     @Param("dni") String dni,
                                     @Param("email") String email,
                                     Long businessId,
                                     Pageable pageable);

    Optional<Customer> findByIdAndBusiness_Id(Long id, Long businessId);
    Customer findByDniAndBusiness_Id(String dni, Long businessId);
}
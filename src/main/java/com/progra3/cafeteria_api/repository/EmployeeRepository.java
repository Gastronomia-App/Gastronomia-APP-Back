package com.progra3.cafeteria_api.repository;

import com.progra3.cafeteria_api.model.entity.Employee;
import com.progra3.cafeteria_api.model.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee>{
    Optional<Employee> findByIdAndBusiness_Id(Long id, Long businessId);

    @Query("SELECT e FROM Employee e WHERE " +
            "e.business.id = :businessId AND " +
            "e.deleted = false AND" +
            "(:name IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:lastName IS NULL OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) AND " +
            "(:dni IS NULL OR e.dni LIKE CONCAT('%', :dni, '%')) AND " +
            "(:email IS NULL OR LOWER(e.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
            "(:role IS NULL OR e.role = :role) ")
    Page<Employee> findByBusiness_Id(@Param("name") String name,
                                     @Param("lastName") String lastName,
                                     @Param("dni") String dni,
                                     @Param("email") String email,
                                     @Param("role") Role role,
                                     Long businessId,
                                     Pageable pageable);

    Optional<Employee> findByUsername(String username);
    Optional<Employee> findByEmailAndBusiness_Id(String email, Long businessId);
    Employee findByDniAndBusiness_Id(String dni, Long businessId);

    boolean existsByRoleAndBusiness_Id(Role role, Long businessId);
    boolean existsByDniAndBusiness_Id(String dni, Long businessId);
}

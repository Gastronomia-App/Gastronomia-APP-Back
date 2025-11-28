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
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByIdAndBusiness_Id(Long id, Long businessId);

    @Query("SELECT e FROM Employee e WHERE " +
            "e.business.id = :businessId AND " +
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

    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.business WHERE e.username = :username")
    Optional<Employee> findByUsernameWithBusiness(@Param("username") String username);

    Optional<Employee> findByEmailAndBusiness_Id(String email, Long businessId);

    Employee findByDniAndBusiness_Id(String dni, Long businessId);

    boolean existsByRoleAndBusiness_Id(Role role, Long businessId);

    boolean existsByDniAndBusiness_Id(String dni, Long businessId);

    boolean existsByEmailAndBusiness_Id(String email, Long businessId);

    boolean existsByPhoneNumberAndBusiness_Id(String phoneNumber, Long businessId);

    boolean existsByUsername(String username);

    // === Uniqueness checks for update (ignore same employee) ===

    boolean existsByDniAndBusiness_IdAndIdNot(String dni, Long businessId, Long id);

    boolean existsByEmailAndBusiness_IdAndIdNot(String email, Long businessId, Long id);


    boolean existsByPhoneNumberAndBusiness_IdAndIdNot(String phoneNumber, Long businessId, Long id);

    // === Username base (before '@') uniqueness per business ===

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN TRUE ELSE FALSE END " +
            "FROM Employee e " +
            "WHERE e.business.id = :businessId " +
            "AND e.username LIKE CONCAT(:baseUsername, '@%')")
    boolean existsByBaseUsernameAndBusiness(@Param("baseUsername") String baseUsername,
                                            @Param("businessId") Long businessId);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN TRUE ELSE FALSE END " +
            "FROM Employee e " +
            "WHERE e.business.id = :businessId " +
            "AND e.id <> :employeeId " +
            "AND e.username LIKE CONCAT(:baseUsername, '@%')")
    boolean existsByBaseUsernameAndBusinessAndIdNot(@Param("baseUsername") String baseUsername,
                                                    @Param("businessId") Long businessId,
                                                    @Param("employeeId") Long employeeId);

    long countByBusiness_IdAndDeletedTrueAndRoleNot(Long businessId, Role role);

    long countByBusiness_IdAndRoleNot(Long businessId, Role role);

    long countByBusiness_IdAndDeletedFalseAndRoleNot(Long businessId, Role role);
}
package com.progra3.cafeteria_api.repository;

import com.progra3.cafeteria_api.model.entity.Order;
import com.progra3.cafeteria_api.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByIdAndBusiness_Id(Long orderId, Long businessId);

    @Query("SELECT o FROM Order o WHERE " +
            "o.business.id = :businessId AND " +
            "(:customerId IS NULL OR o.customer.id = :customerId) AND " +
            "(:employeeId IS NULL OR o.employee.id = :employeeId) AND " +
            "(:startDate IS NULL OR o.dateTime >= :startDate) AND " +
            "(:endDate IS NULL OR o.dateTime <= :endDate) AND " +
            "(:status IS NULL OR o.status = :status) ")
    Page<Order> findByBusiness_Id(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate,
                                    @Param("customerId") Long customerId,
                                    @Param("employeeId") Long employeeId,
                                    @Param("status") OrderStatus status,
                                    @Param("businessId") Long businessId,
                                    Pageable pageable);

    List<Order> findByEmployee_IdAndBusiness_Id(Long employeeId, Long businessId);

    List<Order> findByCustomer_IdAndBusiness_Id(Long customerId, Long businessId);

    Optional<Order> findBySeating_IdAndStatusAndBusiness_Id(Long seatingId, OrderStatus orderStatus, Long businessId);

    List<Order> findByDateTimeBetweenAndBusiness_Id(LocalDateTime start, LocalDateTime end, Long businessId);
}

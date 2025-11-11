package com.progra3.cafeteria_api.repository;

import com.progra3.cafeteria_api.model.entity.Order;
import com.progra3.cafeteria_api.model.enums.OrderStatus;
import com.progra3.cafeteria_api.model.enums.OrderType;
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
            "(:customerName IS NULL OR LOWER(CONCAT(o.customer.name, ' ', o.customer.lastName)) LIKE LOWER(CONCAT('%', :customerName, '%'))) AND " +
            "(:employeeName IS NULL OR LOWER(CONCAT(o.employee.name, ' ', o.employee.lastName)) LIKE LOWER(CONCAT('%', :employeeName, '%'))) AND " +
            "(:startDate IS NULL OR o.dateTime >= :startDate) AND " +
            "(:endDate IS NULL OR o.dateTime <= :endDate) AND " +
            "(:status IS NULL OR o.status = :status) AND " +
            "(:seatingNumber IS NULL OR o.seating.number = :seatingNumber) AND " +
            "(:orderType IS NULL OR o.type = :orderType) AND " +
            "(:minTotal IS NULL OR o.total >= :minTotal) AND " +
            "(:maxTotal IS NULL OR o.total <= :maxTotal)")
    Page<Order> findByBusiness_Id(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate,
                                    @Param("customerName") String customerName,
                                    @Param("employeeName") String employeeName,
                                    @Param("status") OrderStatus status,
                                    @Param("seatingNumber") Integer seatingNumber,
                                    @Param("orderType") OrderType orderType,
                                    @Param("minTotal") Double minTotal,
                                    @Param("maxTotal") Double maxTotal,
                                    @Param("businessId") Long businessId,
                                    Pageable pageable);

    List<Order> findByEmployee_IdAndBusiness_Id(Long employeeId, Long businessId);

    List<Order> findByCustomer_IdAndBusiness_Id(Long customerId, Long businessId);

    Optional<Order> findBySeating_IdAndStatusAndBusiness_Id(Long seatingId, OrderStatus orderStatus, Long businessId);

    List<Order> findByDateTimeBetweenAndBusiness_Id(LocalDateTime start, LocalDateTime end, Long businessId);
}

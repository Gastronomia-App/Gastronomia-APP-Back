package com.progra3.cafeteria_api.repository;

import com.progra3.cafeteria_api.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE " +
            "p.business.id = :businessId AND " +
            "p.deleted = false AND" +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(:minStock IS NULL OR p.stock <= :minStock) AND " +
            "(:maxStock IS NULL OR p.stock >= :maxStock) AND " +
            "(:composite IS NULL OR p.composite = :composite) ")
    Page<Product> findByBusiness_Id(@Param("name") String name,
                                    @Param("categoryId") Long categoryId,
                                    @Param("minStock") Integer minStock,
                                    @Param("maxStock") Integer maxStock,
                                    @Param("composite") Boolean composite,
                                    @Param("businessId") Long businessId,
                                    Pageable pageable);

    @EntityGraph(attributePaths = {
            "components",
            "components.product"
    })
    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.business.id = :businessId")
    Optional<Product> findByIdAndBusiness_IdWithComponents(@Param("id") Long id,
                                                           @Param("businessId") Long businessId);
}

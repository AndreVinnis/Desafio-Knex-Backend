package com.uepb.DesafioKnex.repository;

import com.uepb.DesafioKnex.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByClient_id(Long userId);

    @Query("""
        SELECT DISTINCT o
        FROM Order o
        JOIN o.items i
        WHERE i.product.id = :productId
    """)
    List<Order> findAllByProductId(@Param("productId") Long productId);
}

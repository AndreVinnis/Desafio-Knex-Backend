package com.uepb.DesafioKnex.repository;

import com.uepb.DesafioKnex.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}

package com.uepb.DesafioKnex.repository;

import com.uepb.DesafioKnex.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findById(Long id);

    Optional<CartItem> findByCart_IdAndProduct_Id(Long cartId, Long productId);

}

package com.uepb.DesafioKnex.repository;

import com.uepb.DesafioKnex.model.Cart;
import com.uepb.DesafioKnex.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findById(Long id);

    Optional<Cart> findByClientId(Long id);
}

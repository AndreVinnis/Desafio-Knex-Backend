package com.uepb.DesafioKnex.controllers;

import com.uepb.DesafioKnex.dto.request.AddToCartRequest;
import com.uepb.DesafioKnex.dto.request.UpdateCartItemRequest;
import com.uepb.DesafioKnex.dto.response.CartResponse;
import com.uepb.DesafioKnex.model.User;
import com.uepb.DesafioKnex.services.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<CartResponse> getMyCart(@AuthenticationPrincipal User user) {
        CartResponse response = cartService.getMyCart(user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/items")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<CartResponse> addItem(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AddToCartRequest request) {
        CartResponse response = cartService.addItem(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/items/{productId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @AuthenticationPrincipal User user,
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        CartResponse response = cartService.updateItemQuantity(user, productId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{productId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<CartResponse> removeItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long productId) {
        CartResponse response = cartService.removeItem(user, productId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<CartResponse> clearMyCart(@AuthenticationPrincipal User user) {
        CartResponse response = cartService.clearMyCart(user);
        return ResponseEntity.ok(response);
    }
}
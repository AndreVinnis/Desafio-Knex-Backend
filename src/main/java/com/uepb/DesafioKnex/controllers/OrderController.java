package com.uepb.DesafioKnex.controllers;

import com.uepb.DesafioKnex.dto.request.FinishPurchaseRequest;
import com.uepb.DesafioKnex.dto.response.OrderResponse;
import com.uepb.DesafioKnex.model.User;
import com.uepb.DesafioKnex.model.enums.PaymentMethod;
import com.uepb.DesafioKnex.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/purchase")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<OrderResponse> finishPurchase(
            @AuthenticationPrincipal User user,
            @RequestBody FinishPurchaseRequest request)
    {
        OrderResponse response = orderService.finishPurchase(user, request.paymentMethod());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<OrderResponse>> getMyOrders(@AuthenticationPrincipal User user) {
        List<OrderResponse> response = orderService.getMyOrders(user);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> response = orderService.getAllOrders();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<List<OrderResponse>> getAllByProductId(@PathVariable Long productId) {
        List<OrderResponse> response = orderService.getAllByProductId(productId);
        return ResponseEntity.ok(response);
    }
}
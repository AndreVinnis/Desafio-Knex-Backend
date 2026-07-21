package com.uepb.DesafioKnex.services;

import com.uepb.DesafioKnex.dto.response.OrderItemResponse;
import com.uepb.DesafioKnex.dto.response.OrderResponse;
import com.uepb.DesafioKnex.exceptions.EmptyCart;
import com.uepb.DesafioKnex.exceptions.InsufficientStockException;
import com.uepb.DesafioKnex.model.*;
import com.uepb.DesafioKnex.model.enums.PaymentMethod;
import com.uepb.DesafioKnex.repository.CartRepository;
import com.uepb.DesafioKnex.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    @Transactional
    public OrderResponse finishPurchase(User user, PaymentMethod paymentMethod){
        Cart cart = cartRepository.findByClientId(user.getId()).orElse(null);
        if(cart == null || cart.getItems().isEmpty()){
            throw new EmptyCart();
        }
        Order order = Order.builder()
                .client(user)
                .paymentMethod(paymentMethod)
                .totalAmount(BigDecimal.ZERO)
                .build();
        order = orderRepository.save(order);

        List<OrderItem> orderItems = validateCartItems(cart, order);
        BigDecimal totalAmount = BigDecimal.ZERO;
        for(OrderItem item: orderItems){
            totalAmount = totalAmount.add(item.getUniquePrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        order.setTotalAmount(totalAmount);
        order.setItems(orderItems);
        cartService.clearMyCart(user);
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public List<OrderResponse> getMyOrders(User user){
        List<Order> orders = orderRepository.findByClient_id(user.getId());
        List<OrderResponse> orderResponses = new ArrayList<>();
        for(Order order: orders){
            orderResponses.add(toResponse(order));
        }
        return orderResponses;
    }

    @Transactional
    public List<OrderResponse> getAllOrders(){
        List<Order> orders = orderRepository.findAll();
        List<OrderResponse> orderResponses = new ArrayList<>();
        for(Order order: orders){
            orderResponses.add(toResponse(order));
        }
        return orderResponses;
    }

    @Transactional
    public List<OrderResponse> getAllByProductId(Long productId){
        List<Order> orders = orderRepository.findAllByProductId(productId);
        List<OrderResponse> orderResponses = new ArrayList<>();
        for(Order order: orders){
            orderResponses.add(toResponse(order));
        }
        return orderResponses;
    }

    private List<OrderItem> validateCartItems(Cart cart, Order order){
        List<OrderItem> orderItems = new ArrayList<>();
        for(CartItem item: cart.getItems()){
            Product product = productService.getProductById(item.getProduct().getId());
            productService.decrementsStockByPurchase(product, item.getQuantity());
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(item.getQuantity())
                    .uniquePrice(product.getPrice())
                    .build();
            orderItems.add(orderItem);
        }
        return orderItems;
    }

    private OrderResponse toResponse(Order order){
        List<OrderItemResponse> orderItemResponses = new ArrayList<>();
        for(OrderItem item: order.getItems()){
            orderItemResponses.add(new OrderItemResponse(
                    item.getId(),
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getUniquePrice(),
                    item.getUniquePrice().multiply(BigDecimal.valueOf(item.getQuantity()))
            ));
        }
        return new OrderResponse(order.getId(), order.getTotalAmount(), orderItemResponses);
    }
}

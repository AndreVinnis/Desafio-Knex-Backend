package com.uepb.DesafioKnex.services;

import com.uepb.DesafioKnex.dto.response.OrderResponse;
import com.uepb.DesafioKnex.exceptions.EmptyCart;
import com.uepb.DesafioKnex.model.*;
import com.uepb.DesafioKnex.model.enums.PaymentMethod;
import com.uepb.DesafioKnex.repository.CartRepository;
import com.uepb.DesafioKnex.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartService cartService;

    @Mock
    private ProductService productService;

    private User mockUser;
    private Cart mockCart;
    private CartItem mockCartItem;
    private Product mockProduct;

    @BeforeEach
    void setUp() {
        mockUser = mock(User.class);
        lenient().when(mockUser.getId()).thenReturn(1L);

        mockProduct = mock(Product.class);
        lenient().when(mockProduct.getId()).thenReturn(100L);
        lenient().when(mockProduct.getName()).thenReturn("Notebook");
        lenient().when(mockProduct.getPrice()).thenReturn(new BigDecimal("3000.00"));

        mockCartItem = mock(CartItem.class);
        lenient().when(mockCartItem.getProduct()).thenReturn(mockProduct);
        lenient().when(mockCartItem.getQuantity()).thenReturn(2);

        mockCart = mock(Cart.class);
        lenient().when(mockCart.getItems()).thenReturn(List.of(mockCartItem));
    }

    @Test
    void finishPurchase_Success() {
        // Arrange
        when(cartRepository.findByClientId(mockUser.getId())).thenReturn(Optional.of(mockCart));
        when(productService.getProductById(100L)).thenReturn(mockProduct);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            return savedOrder;
        });

        // Act
        OrderResponse response = orderService.finishPurchase(mockUser, PaymentMethod.PIX);

        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("6000.00"), response.totalAmount()); // 2 * 3000
        assertEquals(1, response.items().size());
        assertEquals("Notebook", response.items().get(0).productName());
        verify(productService, times(1)).decrementsStockByPurchase(mockProduct, 2);
        verify(cartService, times(1)).clearMyCart(mockUser);
        verify(orderRepository, times(2)).save(any(Order.class));
    }

    @Test
    void finishPurchase_ThrowsEmptyCart_WhenCartIsNull() {
        // Arrange
        when(cartRepository.findByClientId(mockUser.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EmptyCart.class, () -> orderService.finishPurchase(mockUser, PaymentMethod.CREDIT_CARD));

        // Verify no side effects
        verify(orderRepository, never()).save(any(Order.class));
        verify(cartService, never()).clearMyCart(any());
    }

    @Test
    void finishPurchase_ThrowsEmptyCart_WhenCartIsEmpty() {
        // Arrange
        Cart emptyCart = mock(Cart.class);
        when(emptyCart.getItems()).thenReturn(Collections.emptyList());
        when(cartRepository.findByClientId(mockUser.getId())).thenReturn(Optional.of(emptyCart));

        // Act & Assert
        assertThrows(EmptyCart.class, () -> orderService.finishPurchase(mockUser, PaymentMethod.PIX));
    }

    @Test
    void getMyOrders_Success() {
        // Arrange
        Order mockOrder = createMockOrderWithItems();
        when(orderRepository.findByClient_id(mockUser.getId())).thenReturn(List.of(mockOrder));

        // Act
        List<OrderResponse> responses = orderService.getMyOrders(mockUser);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(new BigDecimal("3000.00"), responses.get(0).totalAmount());
        verify(orderRepository, times(1)).findByClient_id(mockUser.getId());
    }

    @Test
    void getAllOrders_Success() {
        // Arrange
        Order mockOrder = createMockOrderWithItems();
        when(orderRepository.findAll()).thenReturn(List.of(mockOrder));

        // Act
        List<OrderResponse> responses = orderService.getAllOrders();

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void getAllByProductId_Success() {
        // Arrange
        Long productId = 100L;
        Order mockOrder = createMockOrderWithItems();
        when(orderRepository.findAllByProductId(productId)).thenReturn(List.of(mockOrder));

        // Act
        List<OrderResponse> responses = orderService.getAllByProductId(productId);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(orderRepository, times(1)).findAllByProductId(productId);
    }

    private Order createMockOrderWithItems() {
        Order order = mock(Order.class);
        OrderItem orderItem = mock(OrderItem.class);

        lenient().when(order.getId()).thenReturn(1L);
        lenient().when(order.getTotalAmount()).thenReturn(new BigDecimal("3000.00"));
        lenient().when(orderItem.getId()).thenReturn(10L);
        lenient().when(orderItem.getProduct()).thenReturn(mockProduct);
        lenient().when(orderItem.getQuantity()).thenReturn(1);
        lenient().when(orderItem.getUniquePrice()).thenReturn(new BigDecimal("3000.00"));
        lenient().when(order.getItems()).thenReturn(List.of(orderItem));
        return order;
    }
}
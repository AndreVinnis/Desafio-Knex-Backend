package com.uepb.DesafioKnex.services;

import com.uepb.DesafioKnex.dto.request.AddToCartRequest;
import com.uepb.DesafioKnex.dto.request.UpdateCartItemRequest;
import com.uepb.DesafioKnex.dto.response.CartResponse;
import com.uepb.DesafioKnex.exceptions.CartItemNotFoundException;
import com.uepb.DesafioKnex.exceptions.InsufficientStockException;
import com.uepb.DesafioKnex.model.Cart;
import com.uepb.DesafioKnex.model.CartItem;
import com.uepb.DesafioKnex.model.Product;
import com.uepb.DesafioKnex.model.User;
import com.uepb.DesafioKnex.repository.CartItemRepository;
import com.uepb.DesafioKnex.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @InjectMocks
    private CartService cartService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductService productService;

    private User user;
    private Product product;
    private Cart cart;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("John Doe").build();

        product = new Product();
        product.setId(100L);
        product.setName("Notebook");
        product.setPrice(new BigDecimal("3000.00"));
        product.setStockQuantity(10);

        cart = Cart.builder()
                .id(10L)
                .client(user)
                .items(new ArrayList<>())
                .build();

        cartItem = CartItem.builder()
                .id(1L)
                .cart(cart)
                .product(product)
                .quantity(2)
                .build();
    }

    @Test
    @DisplayName("Deve retornar o carrinho do usuário se já existir")
    void getMyCart_WhenCartExists_ReturnsCartResponse() {
        when(cartRepository.findByClientId(user.getId())).thenReturn(Optional.of(cart));

        CartResponse response = cartService.getMyCart(user);

        assertNotNull(response);
        assertEquals(cart.getId(), response.cartId());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    @DisplayName("Deve criar e retornar um novo carrinho se não existir")
    void getMyCart_WhenCartDoesNotExist_CreatesAndReturnsCartResponse() {
        when(cartRepository.findByClientId(user.getId())).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponse response = cartService.getMyCart(user);

        assertNotNull(response);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("Deve adicionar um novo item ao carrinho com sucesso")
    void addItem_NewItemSufficientStock_AddsToCart() {
        AddToCartRequest request = new AddToCartRequest(product.getId(), 2);

        when(cartRepository.findByClientId(user.getId())).thenReturn(Optional.of(cart));
        when(productService.getProductById(product.getId())).thenReturn(product);
        when(cartItemRepository.findByCart_IdAndProduct_Id(cart.getId(), product.getId())).thenReturn(Optional.empty());

        CartResponse response = cartService.addItem(user, request);

        assertEquals(1, cart.getItems().size());
        assertEquals(2, cart.getItems().get(0).getQuantity());
        verify(cartRepository).save(cart);
    }

    @Test
    @DisplayName("Deve atualizar a quantidade de um item existente no carrinho")
    void addItem_ExistingItemSufficientStock_UpdatesQuantity() {
        AddToCartRequest request = new AddToCartRequest(product.getId(), 3);
        cart.getItems().add(cartItem);

        when(cartRepository.findByClientId(user.getId())).thenReturn(Optional.of(cart));
        when(productService.getProductById(product.getId())).thenReturn(product);
        when(cartItemRepository.findByCart_IdAndProduct_Id(cart.getId(), product.getId())).thenReturn(Optional.of(cartItem));

        cartService.addItem(user, request);

        assertEquals(5, cartItem.getQuantity());
        verify(cartRepository).save(cart);
    }

    @Test
    @DisplayName("Deve lançar exceção ao adicionar item sem estoque suficiente")
    void addItem_InsufficientStock_ThrowsException() {
        AddToCartRequest request = new AddToCartRequest(product.getId(), 15);

        when(cartRepository.findByClientId(user.getId())).thenReturn(Optional.of(cart));
        when(productService.getProductById(product.getId())).thenReturn(product);

        assertThrows(InsufficientStockException.class, () -> cartService.addItem(user, request));
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    @DisplayName("Deve atualizar a quantidade do item diretamente")
    void updateItemQuantity_ValidQuantity_UpdatesItem() {
        UpdateCartItemRequest request = new UpdateCartItemRequest(5);
        cart.getItems().add(cartItem);

        when(cartRepository.findByClientId(user.getId())).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCart_IdAndProduct_Id(cart.getId(), product.getId())).thenReturn(Optional.of(cartItem));
        when(productService.getProductById(product.getId())).thenReturn(product);

        cartService.updateItemQuantity(user, product.getId(), request);

        assertEquals(5, cartItem.getQuantity());
        verify(cartRepository).save(cart);
    }

    @Test
    @DisplayName("Deve remover o item se a nova quantidade for zero ao atualizar")
    void updateItemQuantity_ZeroQuantity_RemovesItem() {
        UpdateCartItemRequest request = new UpdateCartItemRequest(0);
        cart.getItems().add(cartItem);

        when(cartRepository.findByClientId(user.getId())).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCart_IdAndProduct_Id(cart.getId(), product.getId())).thenReturn(Optional.of(cartItem));

        cartService.updateItemQuantity(user, product.getId(), request);

        assertTrue(cart.getItems().isEmpty());
        verify(cartRepository).save(cart);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar item não existente")
    void updateItemQuantity_ItemNotFound_ThrowsException() {
        UpdateCartItemRequest request = new UpdateCartItemRequest(5);

        when(cartRepository.findByClientId(user.getId())).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCart_IdAndProduct_Id(cart.getId(), product.getId())).thenReturn(Optional.empty());

        assertThrows(CartItemNotFoundException.class, () -> cartService.updateItemQuantity(user, product.getId(), request));
    }

    @Test
    @DisplayName("Deve remover um item do carrinho com sucesso")
    void removeItem_ExistingItem_RemovesFromCart() {
        cart.getItems().add(cartItem);

        when(cartRepository.findByClientId(user.getId())).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCart_IdAndProduct_Id(cart.getId(), product.getId())).thenReturn(Optional.of(cartItem));

        cartService.removeItem(user, product.getId());

        assertTrue(cart.getItems().isEmpty());
        verify(cartRepository).save(cart);
    }

    @Test
    @DisplayName("Deve limpar todos os itens do carrinho")
    void clearMyCart_CartExists_ClearsItems() {
        cart.getItems().add(cartItem);

        when(cartRepository.findByClientId(user.getId())).thenReturn(Optional.of(cart));

        CartResponse response = cartService.clearMyCart(user);

        assertTrue(cart.getItems().isEmpty());
        assertEquals(BigDecimal.ZERO, response.subtotal());
        verify(cartRepository).save(cart);
    }
}
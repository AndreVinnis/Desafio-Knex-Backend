package com.uepb.DesafioKnex.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uepb.DesafioKnex.dto.request.AddToCartRequest;
import com.uepb.DesafioKnex.dto.request.UpdateCartItemRequest;
import com.uepb.DesafioKnex.dto.response.CartItemResponse;
import com.uepb.DesafioKnex.dto.response.CartResponse;
import com.uepb.DesafioKnex.model.User;
import com.uepb.DesafioKnex.model.enums.Role;
import com.uepb.DesafioKnex.security.JwtService;
import com.uepb.DesafioKnex.services.AuthService;
import com.uepb.DesafioKnex.services.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
class CartControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AuthService authService;

    private User clientUser;
    private User sellerUser;
    private CartResponse response;
    private AddToCartRequest addToCartRequest;

    @BeforeEach
    void setUp() {
        clientUser = new User();
        clientUser.setId(1L);
        clientUser.setEmail("client@test.com");
        clientUser.setRole(Role.CLIENT);

        sellerUser = new User();
        sellerUser.setId(2L);
        sellerUser.setEmail("seller@test.com");
        sellerUser.setRole(Role.SELLER);

        response = new CartResponse(
                1L,
                new BigDecimal("139.97"),
                List.of(
                        new CartItemResponse(
                                "Teclado Mecânico",
                                1,
                                new BigDecimal("99.99"),
                                new BigDecimal("99.99")
                        ),
                        new CartItemResponse(
                                "Mouse Gamer",
                                2,
                                new BigDecimal("19.99"),
                                new BigDecimal("39.98")
                        )
                )
        );
        addToCartRequest = new AddToCartRequest(
                1L,
                2
        );
    }

    // ---------------------- GET /cart ----------------------

    @Test
    void getMyCart_deveRetornar200_quandoUsuarioClienteAutenticado() throws Exception {
        when(cartService.getMyCart(any(User.class))).thenReturn(response);

        mockMvc.perform(get("/cart")
                        .with(SecurityMockMvcRequestPostProcessors.user(clientUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1L));

        verify(cartService).getMyCart(any(User.class));
    }

    @Test
    void getMyCart_deveRetornar403_quandoUsuarioNaoForCliente() throws Exception {
        mockMvc.perform(get("/cart")
                        .with(SecurityMockMvcRequestPostProcessors.user(sellerUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyCart_deveRetornar401_quandoNaoAutenticado() throws Exception {
        mockMvc.perform(get("/cart"))
                .andExpect(status().isUnauthorized());
    }

    // ---------------------- POST /cart/items ----------------------

    @Test
    void addItem_deveRetornar201_quandoUsuarioClienteAutenticado() throws Exception {
        when(cartService.addItem(any(User.class), any(AddToCartRequest.class))).thenReturn(response);

        mockMvc.perform(post("/cart/items")
                        .with(SecurityMockMvcRequestPostProcessors.user(clientUser))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addToCartRequest)))
                .andExpect(status().isCreated());

        verify(cartService).addItem(any(User.class), any(AddToCartRequest.class));
    }

    @Test
    void addItem_deveRetornar403_quandoUsuarioNaoForCliente() throws Exception {
        AddToCartRequest request = new AddToCartRequest(10L, 2);

        mockMvc.perform(post("/cart/items")
                        .with(SecurityMockMvcRequestPostProcessors.user(sellerUser))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void addItem_deveRetornar401_quandoNaoAutenticado() throws Exception {
        AddToCartRequest request = new AddToCartRequest(10L, 2);

        mockMvc.perform(post("/cart/items")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ---------------------- PUT /cart/items/{productId} ----------------------

    @Test
    void updateItemQuantity_deveRetornar200_quandoUsuarioClienteAutenticado() throws Exception {
        UpdateCartItemRequest request = new UpdateCartItemRequest(5);
        response = new CartResponse(1L, BigDecimal.TEN, List.of(
                new CartItemResponse(
                        "Teclado Mecânico",
                        5,
                        new BigDecimal("99.99"),
                        new BigDecimal("99.99")
                )
        ));

        when(cartService.updateItemQuantity(any(User.class), eq(10L), any(UpdateCartItemRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/cart/items/{productId}", 10L)
                        .with(SecurityMockMvcRequestPostProcessors.user(clientUser))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(cartService).updateItemQuantity(any(User.class), eq(10L), any(UpdateCartItemRequest.class));
    }

    @Test
    void updateItemQuantity_deveRetornar403_quandoUsuarioNaoForCliente() throws Exception {
        UpdateCartItemRequest request = new UpdateCartItemRequest(5);

        mockMvc.perform(put("/cart/items/{productId}", 10L)
                        .with(SecurityMockMvcRequestPostProcessors.user(sellerUser))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateItemQuantity_deveRetornar401_quandoNaoAutenticado() throws Exception {
        UpdateCartItemRequest request = new UpdateCartItemRequest(5);

        mockMvc.perform(put("/cart/items/{productId}", 10L)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ---------------------- DELETE /cart/items/{productId} ----------------------

    @Test
    void removeItem_deveRetornar200_quandoUsuarioClienteAutenticado() throws Exception {
        response = new CartResponse(1L, BigDecimal.ZERO, null);

        when(cartService.removeItem(any(User.class), eq(10L))).thenReturn(response);

        mockMvc.perform(delete("/cart/items/{productId}", 10L)
                        .with(SecurityMockMvcRequestPostProcessors.user(clientUser))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk());

        verify(cartService).removeItem(any(User.class), eq(10L));
    }

    @Test
    void removeItem_deveRetornar403_quandoUsuarioNaoForCliente() throws Exception {
        mockMvc.perform(delete("/cart/items/{productId}", 10L)
                        .with(SecurityMockMvcRequestPostProcessors.user(sellerUser))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void removeItem_deveRetornar401_quandoNaoAutenticado() throws Exception {
        mockMvc.perform(delete("/cart/items/{productId}", 10L)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized());
    }

    // ---------------------- DELETE /cart ----------------------

    @Test
    void clearMyCart_deveRetornar200_quandoUsuarioClienteAutenticado() throws Exception {
        CartResponse response = new CartResponse(1L, BigDecimal.ZERO, null);

        when(cartService.clearMyCart(any(User.class))).thenReturn(response);

        mockMvc.perform(delete("/cart")
                        .with(SecurityMockMvcRequestPostProcessors.user(clientUser))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk());

        verify(cartService).clearMyCart(any(User.class));
    }

    @Test
    void clearMyCart_deveRetornar403_quandoUsuarioNaoForCliente() throws Exception {
        mockMvc.perform(delete("/cart")
                        .with(SecurityMockMvcRequestPostProcessors.user(sellerUser))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void clearMyCart_deveRetornar401_quandoNaoAutenticado() throws Exception {
        mockMvc.perform(delete("/cart")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized());
    }
}
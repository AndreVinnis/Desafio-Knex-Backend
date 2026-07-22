package com.uepb.DesafioKnex.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uepb.DesafioKnex.dto.request.FinishPurchaseRequest;
import com.uepb.DesafioKnex.dto.response.OrderResponse;
import com.uepb.DesafioKnex.model.User;
import com.uepb.DesafioKnex.model.enums.PaymentMethod;
import com.uepb.DesafioKnex.model.enums.Role;
import com.uepb.DesafioKnex.security.JwtService;
import com.uepb.DesafioKnex.services.AuthService;
import com.uepb.DesafioKnex.services.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AuthService authService;

    private User clientUser;
    private User sellerUser;

    @BeforeEach
    void setUp() {
        // Ajuste conforme os construtores/setters reais da sua classe User
        clientUser = new User();
        clientUser.setId(1L);
        clientUser.setEmail("client@test.com");
        clientUser.setRole(Role.CLIENT);

        sellerUser = new User();
        sellerUser.setId(2L);
        sellerUser.setEmail("seller@test.com");
        sellerUser.setRole(Role.SELLER);
    }

    // ---------------------- POST /orders/purchase ----------------------

    @Test
    void finishPurchase_deveRetornar201_quandoUsuarioClienteAutenticado() throws Exception {
        FinishPurchaseRequest request = new FinishPurchaseRequest(PaymentMethod.PIX);
        OrderResponse response = new OrderResponse(1L, BigDecimal.TEN, null);

        when(orderService.finishPurchase(any(User.class), eq(PaymentMethod.PIX)))
                .thenReturn(response);

        mockMvc.perform(post("/orders/purchase")
                        .with(SecurityMockMvcRequestPostProcessors.user(clientUser))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(orderService).finishPurchase(any(User.class), eq(PaymentMethod.PIX));
    }

    @Test
    void finishPurchase_deveRetornar403_quandoUsuarioNaoForCliente() throws Exception {
        FinishPurchaseRequest request = new FinishPurchaseRequest(PaymentMethod.PIX);

        mockMvc.perform(post("/orders/purchase")
                        .with(SecurityMockMvcRequestPostProcessors.user(sellerUser))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void finishPurchase_deveRetornar401_quandoNaoAutenticado() throws Exception {
        FinishPurchaseRequest request = new FinishPurchaseRequest(PaymentMethod.PIX);

        mockMvc.perform(post("/orders/purchase")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ---------------------- GET /orders/me ----------------------

    @Test
    void getMyOrders_deveRetornarListaDePedidos_quandoUsuarioClienteAutenticado() throws Exception {
        List<OrderResponse> orders = List.of(
                new OrderResponse(1L, BigDecimal.TEN, null),
                new OrderResponse(2L, BigDecimal.TEN, null)
        );

        when(orderService.getMyOrders(any(User.class))).thenReturn(orders);

        mockMvc.perform(get("/orders/me")
                        .with(SecurityMockMvcRequestPostProcessors.user(clientUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(orderService).getMyOrders(any(User.class));
    }

    @Test
    void getMyOrders_deveRetornar403_quandoUsuarioNaoForCliente() throws Exception {
        mockMvc.perform(get("/orders/me")
                        .with(SecurityMockMvcRequestPostProcessors.user(sellerUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyOrders_deveRetornar401_quandoNaoAutenticado() throws Exception {
        mockMvc.perform(get("/orders/me"))
                .andExpect(status().isUnauthorized());
    }

    // ---------------------- GET /orders ----------------------

    @Test
    void getAllOrders_deveRetornarListaDePedidos_quandoUsuarioVendedorAutenticado() throws Exception {
        List<OrderResponse> orders = List.of(new OrderResponse(1L, BigDecimal.TEN, null));

        when(orderService.getAllOrders()).thenReturn(orders);

        mockMvc.perform(get("/orders")
                        .with(SecurityMockMvcRequestPostProcessors.user(sellerUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(orderService).getAllOrders();
    }

    @Test
    void getAllOrders_deveRetornar403_quandoUsuarioNaoForVendedor() throws Exception {
        mockMvc.perform(get("/orders")
                        .with(SecurityMockMvcRequestPostProcessors.user(clientUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllOrders_deveRetornar401_quandoNaoAutenticado() throws Exception {
        mockMvc.perform(get("/orders"))
                .andExpect(status().isUnauthorized());
    }

    // ---------------------- GET /orders/product/{productId} ----------------------

    @Test
    void getAllByProductId_deveRetornarListaDePedidos_quandoUsuarioVendedorAutenticado() throws Exception {
        List<OrderResponse> orders = List.of(new OrderResponse(1L, BigDecimal.TEN, null));

        when(orderService.getAllByProductId(10L)).thenReturn(orders);

        mockMvc.perform(get("/orders/product/{productId}", 10L)
                        .with(SecurityMockMvcRequestPostProcessors.user(sellerUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(orderService).getAllByProductId(10L);
    }

    @Test
    void getAllByProductId_deveRetornar403_quandoUsuarioNaoForVendedor() throws Exception {
        mockMvc.perform(get("/orders/product/{productId}", 10L)
                        .with(SecurityMockMvcRequestPostProcessors.user(clientUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllByProductId_deveRetornar401_quandoNaoAutenticado() throws Exception {
        mockMvc.perform(get("/orders/product/{productId}", 10L))
                .andExpect(status().isUnauthorized());
    }
}
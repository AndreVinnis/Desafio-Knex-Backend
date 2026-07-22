package com.uepb.DesafioKnex.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uepb.DesafioKnex.dto.request.ProductRequest;
import com.uepb.DesafioKnex.dto.response.ProductResponse;
import com.uepb.DesafioKnex.model.Product;
import com.uepb.DesafioKnex.model.User;
import com.uepb.DesafioKnex.model.enums.Role;
import com.uepb.DesafioKnex.security.JwtService;
import com.uepb.DesafioKnex.services.AuthService;
import com.uepb.DesafioKnex.services.ProductService;
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

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AuthService authService;

    private User clientUser;
    private User sellerUser;
    private ProductRequest request;
    private ProductResponse response;

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

        request = new ProductRequest(
                "Notebook Dell Inspiron",
                "Notebook com processador Intel Core i5, 16GB RAM e SSD de 512GB",
                new BigDecimal("4599.90"),
                15
        );
        response = new ProductResponse(
                1L,
                "Notebook Dell Inspiron",
                "Notebook com processador Intel Core i5, 16GB RAM e SSD de 512GB",
                new BigDecimal("4599.90"),
                15
        );
    }

    // ---------------------- GET /products ----------------------

    @Test
    void getAllProducts_deveRetornarListaDeProdutos() throws Exception {
        List<ProductResponse> products = List.of(
                response,
                new ProductResponse(
                        2L,
                        "Mouse gamer",
                        "Mouse gamer top",
                        new BigDecimal("100"),
                        15
                )
        );

        when(productService.getAllProducts()).thenReturn(products);

        mockMvc.perform(get("/products")
                        .with(SecurityMockMvcRequestPostProcessors.user(clientUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(productService).getAllProducts();
    }

    @Test
    void getAllProducts_deveRetornar401_quandoNaoAutenticado() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isUnauthorized());
    }

    // ---------------------- GET /products/{id} ----------------------

    @Test
    void getProductById_deveRetornarProduto_quandoExistir() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setName("Produto A");
        product.setPrice(BigDecimal.TEN);

        when(productService.getProductById(1L)).thenReturn(product);

        mockMvc.perform(get("/products/{id}", 1L)
                        .with(SecurityMockMvcRequestPostProcessors.user(clientUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(productService).getProductById(1L);
    }

    @Test
    void getProductById_deveRetornar401_quandoNaoAutenticado() throws Exception {
        mockMvc.perform(get("/products/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }

    // ---------------------- POST /products ----------------------

    @Test
    void createProduct_deveRetornar200_quandoUsuarioVendedorAutenticado() throws Exception {
        when(productService.create(any(ProductRequest.class))).thenReturn(response);

        mockMvc.perform(post("/products")
                        .with(SecurityMockMvcRequestPostProcessors.user(sellerUser))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(productService).create(any(ProductRequest.class));
    }

    @Test
    void createProduct_deveRetornar403_quandoUsuarioNaoForVendedor() throws Exception {
        mockMvc.perform(post("/products")
                        .with(SecurityMockMvcRequestPostProcessors.user(clientUser))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createProduct_deveRetornar401_quandoNaoAutenticado() throws Exception {
        mockMvc.perform(post("/products")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ---------------------- PUT /products/{id} ----------------------

    @Test
    void updateProduct_deveRetornar200_quandoUsuarioVendedorAutenticado() throws Exception {
        when(productService.update(any(ProductRequest.class), eq(1L))).thenReturn(response);

        mockMvc.perform(put("/products/{id}", 1L)
                        .with(SecurityMockMvcRequestPostProcessors.user(sellerUser))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(request.name()));

        verify(productService).update(any(ProductRequest.class), eq(1L));
    }

    @Test
    void updateProduct_deveRetornar403_quandoUsuarioNaoForVendedor() throws Exception {
        mockMvc.perform(put("/products/{id}", 1L)
                        .with(SecurityMockMvcRequestPostProcessors.user(clientUser))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateProduct_deveRetornar401_quandoNaoAutenticado() throws Exception {
        mockMvc.perform(put("/products/{id}", 1L)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ---------------------- DELETE /products/{id} ----------------------

    @Test
    void deleteProduct_deveRetornar200_quandoUsuarioVendedorAutenticado() throws Exception {
        mockMvc.perform(delete("/products/{id}", 1L)
                        .with(SecurityMockMvcRequestPostProcessors.user(sellerUser))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk());

        verify(productService).delete(1L);
    }

    @Test
    void deleteProduct_deveRetornar403_quandoUsuarioNaoForVendedor() throws Exception {
        mockMvc.perform(delete("/products/{id}", 1L)
                        .with(SecurityMockMvcRequestPostProcessors.user(clientUser))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteProduct_deveRetornar401_quandoNaoAutenticado() throws Exception {
        mockMvc.perform(delete("/products/{id}", 1L)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized());
    }
}
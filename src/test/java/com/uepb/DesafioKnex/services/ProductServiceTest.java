package com.uepb.DesafioKnex.services;

import com.uepb.DesafioKnex.dto.request.ProductRequest;
import com.uepb.DesafioKnex.dto.response.ProductResponse;
import com.uepb.DesafioKnex.exceptions.EmptyList;
import com.uepb.DesafioKnex.exceptions.ProductAlreadySold;
import com.uepb.DesafioKnex.exceptions.ProductNotFound;
import com.uepb.DesafioKnex.model.Product;
import com.uepb.DesafioKnex.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        productRequest = new ProductRequest("Notebook", "Notebook Dell", BigDecimal.valueOf(3500.0), 10);

        product = Product.builder()
                .id(1L)
                .name("Notebook")
                .description("Notebook Dell")
                .price(BigDecimal.valueOf(3500.0))
                .stockQuantity(10)
                .alreadySold(false)
                .build();
    }

    @Test
    @DisplayName("Deve criar um produto com sucesso")
    void createProductSuccessfully() {
        // Arrange
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // Act
        ProductResponse response = productService.create(productRequest);

        // Assert
        assertNotNull(response);
        assertEquals(productRequest.name(), response.name());
        assertEquals(productRequest.description(), response.description());
        assertEquals(productRequest.price(), response.price());
        assertEquals(productRequest.stockQuantity(), response.stockQuantity());

        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Deve retornar uma lista de produtos quando houver produtos cadastrados")
    void getAllProductsSuccessfully() {
        // Arrange
        when(productRepository.findAll()).thenReturn(List.of(product));

        // Act
        List<ProductResponse> responses = productService.getAllProducts();

        // Assert
        assertNotNull(responses);
        assertFalse(responses.isEmpty());
        assertEquals(1, responses.size());
        assertEquals(product.getName(), responses.get(0).name());

        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve lançar EmptyList quando não houver produtos cadastrados")
    void getAllProductsThrowsEmptyList() {
        // Arrange
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        EmptyList exception = assertThrows(EmptyList.class, () -> productService.getAllProducts());
        assertEquals("Ainda não existe nenhum produto cadastrado no sistema", exception.getMessage());

        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve buscar um produto por ID com sucesso")
    void getProductByIdSuccessfully() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act
        Product foundProduct = productService.getProductById(1L);

        // Assert
        assertNotNull(foundProduct);
        assertEquals(product.getId(), foundProduct.getId());

        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar ProductNotFound ao buscar produto com ID inexistente")
    void getProductByIdThrowsProductNotFound() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFound.class, () -> productService.getProductById(1L));

        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve atualizar um produto com sucesso")
    void updateProductSuccessfully() {
        // Arrange
        ProductRequest updateRequest = new ProductRequest("Notebook Atualizado", null, BigDecimal.valueOf(4000.0), null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // Act
        ProductResponse response = productService.update(updateRequest, 1L);

        // Assert
        assertNotNull(response);
        assertEquals("Notebook Atualizado", product.getName());
        assertEquals(BigDecimal.valueOf(4000.0), product.getPrice());
        // Garante que campos nulos não substituíram os antigos
        assertEquals("Notebook Dell", product.getDescription());
        assertEquals(10, product.getStockQuantity());

        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(product);
    }

    @Test
    @DisplayName("Deve deletar um produto não vendido com sucesso")
    void deleteProductSuccessfully() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act
        productService.delete(1L);

        // Assert
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).delete(product);
    }

    @Test
    @DisplayName("Deve lançar ProductAlreadySold ao tentar deletar um produto já vendido")
    void deleteProductThrowsProductAlreadySold() {
        // Arrange
        product.setAlreadySold(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act & Assert
        ProductAlreadySold exception = assertThrows(ProductAlreadySold.class, () -> productService.delete(1L));
        assertEquals("Esse produto já foi vendido", exception.getMessage());

        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, never()).delete(any());
    }
}
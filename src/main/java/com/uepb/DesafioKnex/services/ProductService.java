package com.uepb.DesafioKnex.services;

import com.uepb.DesafioKnex.dto.request.ProductRequest;
import com.uepb.DesafioKnex.dto.response.ProductResponse;
import com.uepb.DesafioKnex.exceptions.EmptyList;
import com.uepb.DesafioKnex.exceptions.ProductAlreadySold;
import com.uepb.DesafioKnex.exceptions.ProductNotFound;
import com.uepb.DesafioKnex.model.Product;
import com.uepb.DesafioKnex.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public ProductResponse create(ProductRequest productRequest){
        Product newProduct = Product.builder()
                .name(productRequest.name())
                .description(productRequest.description())
                .price(productRequest.price())
                .stockQuantity(productRequest.stockQuantity())
                .alreadySold(false)
                .build();
        return toResponse(productRepository.save(newProduct));
    }

    @Transactional
    public List<ProductResponse> getAllProducts(){
        String defaultMessage = "Ainda não existe nenhum produto cadastrado no sistema";

        List<Product> products = productRepository.findAll();
        if(products.isEmpty()){
            throw new EmptyList(defaultMessage);
        }
        List<ProductResponse> productResponses = new ArrayList<>();
        for(Product product: products){
            productResponses.add(toResponse(product));
        }
        return productResponses;
    }

    @Transactional
    public Product getProductById(Long id){
        Product product = productRepository.findById(id).orElseThrow(
                () -> new ProductNotFound(id)
        );
        return product;
    }

    @Transactional
    public ProductResponse update(ProductRequest productRequest, Long id){
        Product product = productRepository.findById(id).orElseThrow(
                () -> new ProductNotFound(id)
        );

        if(productRequest.name() != null){
            product.setName(productRequest.name());
        }
        if(productRequest.description() != null){
            product.setDescription(productRequest.description());
        }
        if(productRequest.price() != null){
            product.setPrice(productRequest.price());
        }
        if(productRequest.stockQuantity() != null){
            product.setStockQuantity(productRequest.stockQuantity());
        }

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id){
        String defaultMessage = "Esse produto já foi vendido";
        Product product = productRepository.findById(id).orElseThrow(
                () -> new ProductNotFound(id)
        );
        if(product.getAlreadySold()){
            throw new ProductAlreadySold(defaultMessage);
        }
        productRepository.delete(product);
    }

    private ProductResponse toResponse(Product product){
        return new ProductResponse(
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity()
                );
    }
}

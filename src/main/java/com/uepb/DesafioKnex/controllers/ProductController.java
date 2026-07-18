package com.uepb.DesafioKnex.controllers;

import com.uepb.DesafioKnex.dto.request.ProductRequest;
import com.uepb.DesafioKnex.dto.response.ProductResponse;
import com.uepb.DesafioKnex.model.Product;
import com.uepb.DesafioKnex.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getALLProducts(){
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id){
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductRequest request){
        return ResponseEntity.ok(productService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ProductResponse> updateProduct(@RequestBody ProductRequest request, @PathVariable Long id){
        return ResponseEntity.ok(productService.update(request, id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id){
        productService.delete(id);
        return ResponseEntity.ok().build();
    }

}

package com.oscarr.demoappgym.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oscarr.demoappgym.model.dto.ProductRequest;
import com.oscarr.demoappgym.model.dto.ProductResponse;
import com.oscarr.demoappgym.model.entity.Product;
import com.oscarr.demoappgym.service.ProductService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        Product product = new Product();
        applyRequest(product, request);
        return ResponseEntity.ok(toResponse(productService.save(product)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        Optional<Product> existing = productService.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Product product = existing.get();
        applyRequest(product, request);
        return ResponseEntity.ok(toResponse(productService.save(product)));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProduct() {
        return ResponseEntity.ok(productService.findAll().stream().map(this::toResponse).toList());
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductResponse>> getLowStockProducts() {
        return ResponseEntity.ok(productService.findLowStockProducts().stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return productService.findById(id)
                .map(product -> ResponseEntity.ok(toResponse(product)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void applyRequest(Product product, ProductRequest request) {
        product.setName_Product(request.getNameProduct());
        product.setValue(request.getValue());
        product.setAmount(request.getAmount());
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(product.getCode_Products(), product.getName_Product(),
                product.getValue(), product.getAmount());
    }
}

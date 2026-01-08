package com.ydg.orderstock.controller;

import com.ydg.orderstock.dto.ProductRequest;
import com.ydg.orderstock.dto.ProductResponse;
import com.ydg.orderstock.dto.StockUpdateRequest;
import com.ydg.orderstock.entity.Product;
import com.ydg.orderstock.service.ProductService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> list() {
        List<ProductResponse> products = productService.list().stream()
            .map(this::toResponse)
            .toList();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> get(@PathVariable("id") Long id) {
        return ResponseEntity.ok(toResponse(productService.get(id)));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(toResponse(productService.create(request)));
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductResponse> updateStock(@PathVariable("id") Long id,
                                                       @Valid @RequestBody StockUpdateRequest request) {
        return ResponseEntity.ok(toResponse(productService.updateStock(id, request)));
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(product.getId(), product.getTitle(), product.getPrice(),
            product.getStockQuantity(), product.getCreatedAt());
    }
}

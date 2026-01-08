package com.ydg.orderstock.service;

import com.ydg.orderstock.dto.ProductRequest;
import com.ydg.orderstock.dto.StockUpdateRequest;
import com.ydg.orderstock.entity.Product;
import com.ydg.orderstock.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> list() {
        return productRepository.findAll();
    }

    public Product get(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product not found"));
    }

    public Product create(ProductRequest request) {
        if (request.getStockQuantity() == null || request.getStockQuantity() < 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Stock must be >= 0");
        }
        Product product = new Product();
        product.setTitle(request.getTitle());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        return productRepository.save(product);
    }

    public Product updateStock(Long id, StockUpdateRequest request) {
        Product product = get(id);
        int newStock = product.getStockQuantity() + request.getDelta();
        if (newStock < 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Stock cannot be negative");
        }
        product.setStockQuantity(newStock);
        return productRepository.save(product);
    }
}

package com.ydg.orderstock.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class ProductResponse {
    private Long id;
    private String title;
    private BigDecimal price;
    private int stockQuantity;
    private Instant createdAt;

    public ProductResponse(Long id, String title, BigDecimal price, int stockQuantity, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

package com.ydg.orderstock.dto;

import java.math.BigDecimal;

public class CartItemResponse {
    private Long productId;
    private String title;
    private BigDecimal price;
    private int quantity;

    public CartItemResponse(Long productId, String title, BigDecimal price, int quantity) {
        this.productId = productId;
        this.title = title;
        this.price = price;
        this.quantity = quantity;
    }

    public Long getProductId() {
        return productId;
    }

    public String getTitle() {
        return title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }
}

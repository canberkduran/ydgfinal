package com.ydg.orderstock.dto;

import java.math.BigDecimal;

public class OrderItemResponse {
    private Long productId;
    private String productTitle;
    private BigDecimal unitPrice;
    private int quantity;

    public OrderItemResponse(Long productId, String productTitle, BigDecimal unitPrice, int quantity) {
        this.productId = productId;
        this.productTitle = productTitle;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }
}

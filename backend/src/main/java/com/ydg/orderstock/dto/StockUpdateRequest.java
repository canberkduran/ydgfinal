package com.ydg.orderstock.dto;

import jakarta.validation.constraints.NotNull;

public class StockUpdateRequest {
    @NotNull
    private Integer delta;

    public Integer getDelta() {
        return delta;
    }

    public void setDelta(Integer delta) {
        this.delta = delta;
    }
}

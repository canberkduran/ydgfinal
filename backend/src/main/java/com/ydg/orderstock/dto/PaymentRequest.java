package com.ydg.orderstock.dto;

import jakarta.validation.constraints.NotBlank;

public class PaymentRequest {
    @NotBlank
    private String method;

    private boolean forceFail;

    @NotBlank
    @jakarta.validation.constraints.Pattern(regexp = "\\d{6}", message = "Hatali kart formati")
    private String cardNumber;

    @NotBlank
    @jakarta.validation.constraints.Pattern(regexp = "\\d{3}", message = "Hatali cvc numarasi")
    private String cvc;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public boolean isForceFail() {
        return forceFail;
    }

    public void setForceFail(boolean forceFail) {
        this.forceFail = forceFail;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCvc() {
        return cvc;
    }

    public void setCvc(String cvc) {
        this.cvc = cvc;
    }
}

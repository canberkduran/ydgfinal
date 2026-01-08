package com.ydg.orderstock.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ydg.orderstock.dto.CartItemRequest;
import com.ydg.orderstock.dto.ProductRequest;
import com.ydg.orderstock.dto.RegisterRequest;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;




@SpringBootTest
@AutoConfigureMockMvc
public class CheckoutFlowIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void addToCartAndCheckoutDecreasesStock() throws Exception {
        String token = registerAndGetToken("checkoutuser@example.com");
        Long productId = createProduct(token, "Mouse", new BigDecimal("50.00"), 5);

        CartItemRequest cartItemRequest = new CartItemRequest();
        cartItemRequest.setProductId(productId);
        cartItemRequest.setQuantity(2);

        mockMvc.perform(post("/api/cart/items")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartItemRequest)))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/checkout")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CREATED"));

        mockMvc.perform(get("/api/products/" + productId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stockQuantity").value(3));
    }

    @Test
    void checkoutFailsWhenStockInsufficient() throws Exception {
        String token = registerAndGetToken("stockfail@example.com");
        Long productId = createProduct(token, "Keyboard", new BigDecimal("70.00"), 1);

        CartItemRequest cartItemRequest = new CartItemRequest();
        cartItemRequest.setProductId(productId);
        cartItemRequest.setQuantity(1);

        mockMvc.perform(post("/api/cart/items")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartItemRequest)))
            .andExpect(status().isOk());

        String stockUpdatePayload = """
            {"delta":-1}
            """;

        mockMvc.perform(patch("/api/products/" + productId + "/stock")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(stockUpdatePayload))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/checkout")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isBadRequest());
    }

    @Test
    void payFailsWhenBalanceInsufficient() throws Exception {
        String token = registerAndGetToken("balancefail@example.com");
        Long productId = createProduct(token, "Premium Item", new BigDecimal("400.00"), 2);

        CartItemRequest cartItemRequest = new CartItemRequest();
        cartItemRequest.setProductId(productId);
        cartItemRequest.setQuantity(1);

        mockMvc.perform(post("/api/cart/items")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartItemRequest)))
            .andExpect(status().isOk());

        MvcResult checkoutResult = mockMvc.perform(post("/api/checkout")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode orderNode = objectMapper.readTree(checkoutResult.getResponse().getContentAsString());
        long orderId = orderNode.get("id").asLong();

        String paymentPayload = """
            {"method":"CARD","forceFail":false,"cardNumber":"123456","cvc":"789"}
            """;

        mockMvc.perform(post("/api/orders/" + orderId + "/pay")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(paymentPayload))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Yetersiz bakiye")));
    }

    @Test
    void payFailsWhenCardFormatInvalid() throws Exception {
        String token = registerAndGetToken("cardformat@example.com");
        Long productId = createProduct(token, "Card Item", new BigDecimal("50.00"), 3);

        CartItemRequest cartItemRequest = new CartItemRequest();
        cartItemRequest.setProductId(productId);
        cartItemRequest.setQuantity(1);

        mockMvc.perform(post("/api/cart/items")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartItemRequest)))
            .andExpect(status().isOk());

        MvcResult checkoutResult = mockMvc.perform(post("/api/checkout")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode orderNode = objectMapper.readTree(checkoutResult.getResponse().getContentAsString());
        long orderId = orderNode.get("id").asLong();

        String paymentPayload = """
            {"method":"CARD","forceFail":false,"cardNumber":"12ab56","cvc":"789"}
            """;

        mockMvc.perform(post("/api/orders/" + orderId + "/pay")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(paymentPayload))
            .andExpect(status().isBadRequest());
    }

    @Test
    void addToCartFailsWhenStockInsufficient() throws Exception {
        String token = registerAndGetToken("cartstock@example.com");
        Long productId = createProduct(token, "Limited Item", new BigDecimal("30.00"), 1);

        CartItemRequest cartItemRequest = new CartItemRequest();
        cartItemRequest.setProductId(productId);
        cartItemRequest.setQuantity(2);

        mockMvc.perform(post("/api/cart/items")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartItemRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Stok yetersiz")));
    }

    private String registerAndGetToken(String email) throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setEmail(email);
        register.setName("User");
        register.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("token").asText();
    }

    private Long createProduct(String token, String title, BigDecimal price, int stock) throws Exception {
        ProductRequest request = new ProductRequest();
        request.setTitle(title);
        request.setPrice(price);
        request.setStockQuantity(stock);

        MvcResult result = mockMvc.perform(post("/api/products")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("id").asLong();
    }
}

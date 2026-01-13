package com.ydg.orderstock.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class ProductControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createAndListProduct() throws Exception {
        String token = registerAndGetToken();

        ProductRequest request = new ProductRequest();
        request.setTitle("Laptop");
        request.setPrice(new BigDecimal("1200.00"));
        request.setStockQuantity(10);

        mockMvc.perform(post("/api/products")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists());

        MvcResult listResult = mockMvc.perform(get("/api/products"))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode listNode = objectMapper.readTree(listResult.getResponse().getContentAsString());
        boolean found = false;
        for (JsonNode node : listNode) {
            if (node.get("title").asText().equals("Laptop")) {
                found = true;
                break;
            }
        }
        org.junit.jupiter.api.Assertions.assertTrue(found);
    }




    

    private String registerAndGetToken() throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setEmail("productuser@example.com");
        register.setName("Prod User");
        register.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("token").asText();
    }
}

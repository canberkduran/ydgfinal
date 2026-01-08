package com.ydg.orderstock.controller;

import com.ydg.orderstock.dto.CartItemRequest;
import com.ydg.orderstock.dto.CartItemUpdateRequest;
import com.ydg.orderstock.dto.CartResponse;
import com.ydg.orderstock.security.UserPrincipal;
import com.ydg.orderstock.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(cartService.getCart(principal.getUser()));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@AuthenticationPrincipal UserPrincipal principal,
                                                @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.addItem(principal.getUser(), request));
    }

    @PatchMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateItem(@AuthenticationPrincipal UserPrincipal principal,
                                                   @PathVariable("productId") Long productId,
                                                   @Valid @RequestBody CartItemUpdateRequest request) {
        return ResponseEntity.ok(cartService.updateItem(principal.getUser(), productId, request));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeItem(@AuthenticationPrincipal UserPrincipal principal,
                                                   @PathVariable("productId") Long productId) {
        return ResponseEntity.ok(cartService.removeItem(principal.getUser(), productId));
    }
}

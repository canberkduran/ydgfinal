package com.ydg.orderstock.service;

import com.ydg.orderstock.dto.CartItemRequest;
import com.ydg.orderstock.dto.CartItemResponse;
import com.ydg.orderstock.dto.CartItemUpdateRequest;
import com.ydg.orderstock.dto.CartResponse;
import com.ydg.orderstock.entity.AppUser;
import com.ydg.orderstock.entity.Cart;
import com.ydg.orderstock.entity.CartItem;
import com.ydg.orderstock.entity.Product;
import com.ydg.orderstock.repository.CartRepository;
import com.ydg.orderstock.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    public Cart getOrCreateCart(AppUser user) {
        return cartRepository.findByUser(user).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUser(user);
            return cartRepository.save(cart);
        });
    }

    public CartResponse getCart(AppUser user) {
        Cart cart = getOrCreateCart(user);
        return toResponse(cart);
    }

    public CartResponse addItem(AppUser user, CartItemRequest request) {
        Cart cart = getOrCreateCart(user);
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product not found"));
        if (request.getQuantity() <= 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Quantity must be > 0");
        }

        CartItem existing = cart.getItems().stream()
            .filter(item -> item.getProduct().getId().equals(product.getId()))
            .findFirst()
            .orElse(null);

        int desiredQuantity = request.getQuantity();
        if (existing != null) {
            desiredQuantity = existing.getQuantity() + request.getQuantity();
        }
        if (desiredQuantity > product.getStockQuantity()) {
            throw new ResponseStatusException(BAD_REQUEST, "Stok yetersiz");
        }

        if (existing == null) {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(request.getQuantity());
            cart.getItems().add(item);
        } else {
            existing.setQuantity(existing.getQuantity() + request.getQuantity());
        }
        Cart saved = cartRepository.save(cart);
        return toResponse(saved);
    }

    public CartResponse updateItem(AppUser user, Long productId, CartItemUpdateRequest request) {
        if (request.getQuantity() <= 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Quantity must be > 0");
        }
        Cart cart = getOrCreateCart(user);
        CartItem item = cart.getItems().stream()
            .filter(ci -> ci.getProduct().getId().equals(productId))
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Item not in cart"));
        if (request.getQuantity() > item.getProduct().getStockQuantity()) {
            throw new ResponseStatusException(BAD_REQUEST, "Stok yetersiz");
        }
        item.setQuantity(request.getQuantity());
        Cart saved = cartRepository.save(cart);
        return toResponse(saved);
    }

    public CartResponse removeItem(AppUser user, Long productId) {
        Cart cart = getOrCreateCart(user);
        boolean removed = cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        if (!removed) {
            throw new ResponseStatusException(NOT_FOUND, "Item not in cart");
        }
        Cart saved = cartRepository.save(cart);
        return toResponse(saved);
    }

    public void clearCart(Cart cart) {
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
            .map(item -> new CartItemResponse(
                item.getProduct().getId(),
                item.getProduct().getTitle(),
                item.getProduct().getPrice(),
                item.getQuantity()
            ))
            .toList();
        BigDecimal total = items.stream()
            .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(items, total);
    }
}

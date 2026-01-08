package com.ydg.orderstock.service;

import com.ydg.orderstock.dto.CartItemRequest;
import com.ydg.orderstock.dto.CartItemUpdateRequest;
import com.ydg.orderstock.entity.AppUser;
import com.ydg.orderstock.entity.Cart;
import com.ydg.orderstock.entity.Product;
import com.ydg.orderstock.repository.CartRepository;
import com.ydg.orderstock.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CartServiceTest {
    private CartRepository cartRepository;
    private ProductRepository productRepository;
    private CartService cartService;
    private AppUser user;

    @BeforeEach
    void setup() {
        cartRepository = Mockito.mock(CartRepository.class);
        productRepository = Mockito.mock(ProductRepository.class);
        cartService = new CartService(cartRepository, productRepository);
        user = new AppUser();
        user.setId(1L);
    }

    @Test
    void addItemAddsNewItem() {
        Product product = new Product();
        product.setId(10L);
        product.setTitle("Phone");
        product.setPrice(new BigDecimal("100"));
        product.setStockQuantity(5);

        Cart cart = new Cart();
        cart.setUser(user);

        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CartItemRequest request = new CartItemRequest();
        request.setProductId(10L);
        request.setQuantity(2);

        cartService.addItem(user, request);

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    void updateItemChangesQuantity() {
        Product product = new Product();
        product.setId(10L);
        product.setTitle("Phone");
        product.setPrice(new BigDecimal("100"));

        Cart cart = new Cart();
        cart.setUser(user);

        com.ydg.orderstock.entity.CartItem item = new com.ydg.orderstock.entity.CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(1);
        cart.getItems().add(item);

        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CartItemUpdateRequest request = new CartItemUpdateRequest();
        request.setQuantity(3);

        cartService.updateItem(user, 10L, request);

        assertThat(item.getQuantity()).isEqualTo(3);
    }

    @Test
    void removeItemRemoves() {
        Product product = new Product();
        product.setId(10L);
        product.setTitle("Phone");

        Cart cart = new Cart();
        cart.setUser(user);

        com.ydg.orderstock.entity.CartItem item = new com.ydg.orderstock.entity.CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(1);
        cart.getItems().add(item);

        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        cartService.removeItem(user, 10L);

        assertThat(cart.getItems()).isEmpty();
    }

    @Test
    void addItemRejectsInvalidQuantity() {
        Cart cart = new Cart();
        cart.setUser(user);
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        CartItemRequest request = new CartItemRequest();
        request.setProductId(10L);
        request.setQuantity(0);

        assertThatThrownBy(() -> cartService.addItem(user, request))
            .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void addItemRejectsWhenStockInsufficient() {
        Product product = new Product();
        product.setId(10L);
        product.setTitle("Phone");
        product.setPrice(new BigDecimal("100"));
        product.setStockQuantity(1);

        Cart cart = new Cart();
        cart.setUser(user);

        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        CartItemRequest request = new CartItemRequest();
        request.setProductId(10L);
        request.setQuantity(2);

        assertThatThrownBy(() -> cartService.addItem(user, request))
            .isInstanceOf(ResponseStatusException.class);
    }
}

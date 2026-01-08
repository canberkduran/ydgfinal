package com.ydg.orderstock.service;

import com.ydg.orderstock.dto.PaymentRequest;
import com.ydg.orderstock.entity.*;
import com.ydg.orderstock.repository.CartRepository;
import com.ydg.orderstock.repository.OrderRepository;
import com.ydg.orderstock.repository.ProductRepository;
import com.ydg.orderstock.repository.UserRepository;
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

public class OrderServiceTest {
    private CartRepository cartRepository;
    private OrderRepository orderRepository;
    private ProductRepository productRepository;
    private UserRepository userRepository;
    private OrderService orderService;
    private AppUser user;

    @BeforeEach
    void setup() {
        cartRepository = Mockito.mock(CartRepository.class);
        orderRepository = Mockito.mock(OrderRepository.class);
        productRepository = Mockito.mock(ProductRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        orderService = new OrderService(cartRepository, orderRepository, productRepository, userRepository);
        user = new AppUser();
        user.setId(1L);
        user.setBalance(new BigDecimal("300.00"));
    }

    @Test
    void shouldCreateOrderAndDecreaseStock_whenCheckoutAndStockEnough() {
        Cart cart = new Cart();
        cart.setUser(user);
        Product product = new Product();
        product.setId(11L);
        product.setTitle("Book");
        product.setPrice(new BigDecimal("20.00"));
        product.setStockQuantity(5);

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(2);
        cart.getItems().add(item);

        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productRepository.findById(11L)).thenReturn(Optional.of(product));
        when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Order order = orderService.checkout(user);

        assertThat(order.getItems()).hasSize(1);
        assertThat(product.getStockQuantity()).isEqualTo(3);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("40.00");
    }

    @Test
    void shouldNotCreateOrderAndNotChangeStock_whenStockNotEnough() {
        Cart cart = new Cart();
        cart.setUser(user);
        Product product = new Product();
        product.setId(11L);
        product.setTitle("Book");
        product.setPrice(new BigDecimal("20.00"));
        product.setStockQuantity(1);

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(2);
        cart.getItems().add(item);

        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productRepository.findById(11L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.checkout(user))
            .isInstanceOf(ResponseStatusException.class);
        assertThat(product.getStockQuantity()).isEqualTo(1);
    }

    @Test
    void shouldCancelOrderAndRestoreStock_whenCreatedOrderCancelled() {
        Order order = new Order();
        order.setId(99L);
        order.setUser(user);
        order.setStatus(OrderStatus.CREATED);

        Product product = new Product();
        product.setId(7L);
        product.setTitle("Pen");
        product.setStockQuantity(1);

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProductId(7L);
        item.setProductTitle("Pen");
        item.setUnitPrice(new BigDecimal("2.00"));
        item.setQuantity(3);
        order.getItems().add(item);

        when(orderRepository.findById(99L)).thenReturn(Optional.of(order));
        when(productRepository.findById(7L)).thenReturn(Optional.of(product));
        when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Order cancelled = orderService.cancel(user, 99L);

        assertThat(cancelled.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(product.getStockQuantity()).isEqualTo(4);
    }

    @Test
    void shouldNotShip_whenNotPaid() {
        Order order = new Order();
        order.setId(5L);
        order.setUser(user);
        order.setStatus(OrderStatus.CREATED);

        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.ship(user, 5L))
            .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void shouldPayOrder_whenPaymentSuccess() {
        Order order = new Order();
        order.setId(6L);
        order.setUser(user);
        order.setStatus(OrderStatus.CREATED);
        order.setTotalAmount(new BigDecimal("100.00"));

        PaymentRequest request = new PaymentRequest();
        request.setMethod("CARD");
        request.setForceFail(false);
        request.setCardNumber("123456");
        request.setCvc("789");

        when(orderRepository.findById(6L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Order paid = orderService.pay(user, 6L, request);

        assertThat(paid.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getUser().getBalance()).isEqualByComparingTo("200.00");
    }

    @Test
    void shouldNotChangeStatus_whenPaymentForcedFail() {
        Order order = new Order();
        order.setId(7L);
        order.setUser(user);
        order.setStatus(OrderStatus.CREATED);
        order.setTotalAmount(new BigDecimal("50.00"));

        PaymentRequest request = new PaymentRequest();
        request.setMethod("CARD");
        request.setForceFail(true);
        request.setCardNumber("123456");
        request.setCvc("789");

        when(orderRepository.findById(7L)).thenReturn(Optional.of(order));

        Order result = orderService.pay(user, 7L, request);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void shouldRejectInvalidCardFormat() {
        Order order = new Order();
        order.setId(10L);
        order.setUser(user);
        order.setStatus(OrderStatus.CREATED);
        order.setTotalAmount(new BigDecimal("10.00"));

        PaymentRequest request = new PaymentRequest();
        request.setMethod("CARD");
        request.setCardNumber("12");
        request.setCvc("789");

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.pay(user, 10L, request))
            .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void shouldRejectWrongCardNumber() {
        Order order = new Order();
        order.setId(11L);
        order.setUser(user);
        order.setStatus(OrderStatus.CREATED);
        order.setTotalAmount(new BigDecimal("10.00"));

        PaymentRequest request = new PaymentRequest();
        request.setMethod("CARD");
        request.setCardNumber("999999");
        request.setCvc("789");

        when(orderRepository.findById(11L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.pay(user, 11L, request))
            .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void shouldRejectWrongCvc() {
        Order order = new Order();
        order.setId(12L);
        order.setUser(user);
        order.setStatus(OrderStatus.CREATED);
        order.setTotalAmount(new BigDecimal("10.00"));

        PaymentRequest request = new PaymentRequest();
        request.setMethod("CARD");
        request.setCardNumber("123456");
        request.setCvc("000");

        when(orderRepository.findById(12L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.pay(user, 12L, request))
            .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void shouldRejectInsufficientBalance() {
        Order order = new Order();
        order.setId(13L);
        order.setUser(user);
        order.setStatus(OrderStatus.CREATED);
        order.setTotalAmount(new BigDecimal("350.00"));

        PaymentRequest request = new PaymentRequest();
        request.setMethod("CARD");
        request.setCardNumber("123456");
        request.setCvc("789");

        when(orderRepository.findById(13L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.pay(user, 13L, request))
            .isInstanceOf(ResponseStatusException.class);
    }
}

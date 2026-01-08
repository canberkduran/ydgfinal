package com.ydg.orderstock.service;

import com.ydg.orderstock.dto.PaymentRequest;
import com.ydg.orderstock.entity.*;
import com.ydg.orderstock.repository.CartRepository;
import com.ydg.orderstock.repository.OrderRepository;
import com.ydg.orderstock.repository.ProductRepository;
import com.ydg.orderstock.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class OrderService {
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(CartRepository cartRepository, OrderRepository orderRepository,
                        ProductRepository productRepository, UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Order checkout(AppUser user) {
        Cart cart = cartRepository.findByUser(user)
            .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Cart is empty"));
        if (cart.getItems().isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Cart is empty");
        }

        for (CartItem item : cart.getItems()) {
            Product product = productRepository.findById(item.getProduct().getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product not found"));
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new ResponseStatusException(BAD_REQUEST, "Insufficient stock for product: " + product.getTitle());
            }
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.CREATED);

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cart.getItems()) {
            Product product = productRepository.findById(item.getProduct().getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product not found"));
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(product.getId());
            orderItem.setProductTitle(product.getTitle());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setQuantity(item.getQuantity());
            order.getItems().add(orderItem);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);
        cart.getItems().clear();
        cartRepository.save(cart);
        return saved;
    }

    public Order pay(AppUser user, Long orderId, PaymentRequest request) {
        Order order = getOwnedOrder(user, orderId);
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new ResponseStatusException(BAD_REQUEST, "Only CREATED orders can be paid");
        }
        if (request.isForceFail()) {
            return order;
        }
        if (!request.getCardNumber().matches("\\d{6}")) {
            throw new ResponseStatusException(BAD_REQUEST, "Hatali kart formati");
        }
        if (!"123456".equals(request.getCardNumber())) {
            throw new ResponseStatusException(BAD_REQUEST, "Hatali kart bilgisi");
        }
        if (!request.getCvc().matches("\\d{3}")) {
            throw new ResponseStatusException(BAD_REQUEST, "Hatali cvc numarasi");
        }
        if (!"789".equals(request.getCvc())) {
            throw new ResponseStatusException(BAD_REQUEST, "Hatali cvc numarasi");
        }
        AppUser orderUser = order.getUser();
        if (orderUser.getBalance().compareTo(order.getTotalAmount()) < 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Yetersiz bakiye");
        }
        orderUser.setBalance(orderUser.getBalance().subtract(order.getTotalAmount()));
        userRepository.save(orderUser);
        order.setStatus(OrderStatus.PAID);
        return orderRepository.save(order);
    }

    public Order cancel(AppUser user, Long orderId) {
        Order order = getOwnedOrder(user, orderId);
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new ResponseStatusException(BAD_REQUEST, "Only CREATED orders can be cancelled");
        }
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product not found"));
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }
        order.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    public Order ship(AppUser user, Long orderId) {
        Order order = getOwnedOrder(user, orderId);
        if (order.getStatus() != OrderStatus.PAID) {
            throw new ResponseStatusException(BAD_REQUEST, "Only PAID orders can be shipped");
        }
        order.setStatus(OrderStatus.SHIPPED);
        return orderRepository.save(order);
    }

    public List<Order> listOrders(AppUser user) {
        return orderRepository.findByUser(user);
    }

    public Order getOrder(AppUser user, Long id) {
        return getOwnedOrder(user, id);
    }

    private Order getOwnedOrder(AppUser user, Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Order not found"));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(FORBIDDEN, "Yetkisiz");
        }
        return order;
    }
}

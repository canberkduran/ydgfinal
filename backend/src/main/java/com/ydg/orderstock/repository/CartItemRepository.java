package com.ydg.orderstock.repository;

import com.ydg.orderstock.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}

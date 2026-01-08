package com.ydg.orderstock.repository;

import com.ydg.orderstock.entity.Cart;
import com.ydg.orderstock.entity.AppUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(AppUser user);
}

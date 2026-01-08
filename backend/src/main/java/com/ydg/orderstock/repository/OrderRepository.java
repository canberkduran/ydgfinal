package com.ydg.orderstock.repository;

import com.ydg.orderstock.entity.Order;
import com.ydg.orderstock.entity.AppUser;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(AppUser user);
}

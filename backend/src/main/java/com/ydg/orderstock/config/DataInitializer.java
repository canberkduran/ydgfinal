package com.ydg.orderstock.config;

import com.ydg.orderstock.entity.Product;
import com.ydg.orderstock.repository.ProductRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedProducts(ProductRepository productRepository) {
        return args -> {
            if (productRepository.count() == 0) {
                Product p1 = new Product();
                p1.setTitle("Standard Widget");
                p1.setPrice(new BigDecimal("25.00"));
                p1.setStockQuantity(10);

                Product p2 = new Product();
                p2.setTitle("Limited Widget");
                p2.setPrice(new BigDecimal("40.00"));
                p2.setStockQuantity(1);

                Product p3 = new Product();
                p3.setTitle("Cancelable Item");
                p3.setPrice(new BigDecimal("15.00"));
                p3.setStockQuantity(2);

                Product p4 = new Product();
                p4.setTitle("Expensive Item");
                p4.setPrice(new BigDecimal("350.00"));
                p4.setStockQuantity(2);

                productRepository.save(p1);
                productRepository.save(p2);
                productRepository.save(p3);
                productRepository.save(p4);
            }
        };
    }
}

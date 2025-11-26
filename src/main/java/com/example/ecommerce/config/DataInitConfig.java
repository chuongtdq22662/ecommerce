package com.example.ecommerce.config;

import com.example.ecommerce.entity.Product;
import com.example.ecommerce.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
public class DataInitConfig {

    @Bean
    public CommandLineRunner initData(ProductRepository productRepository) {
        return args -> {
            if (productRepository.count() == 0) { // chỉ insert nếu DB trống
                Product p = Product.builder()
                        .name("Sample Product")
                        .description("This is a sample product")
                        .price(new BigDecimal("199000"))
                        .stock(100)
                        .createdAt(LocalDateTime.now())
                        .build();

                productRepository.save(p);
                System.out.println("✅ Insert sample product thành công!");
            }
        };
    }
}

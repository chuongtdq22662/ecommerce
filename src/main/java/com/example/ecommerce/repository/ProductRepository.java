package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // sau này mình sẽ thêm method custom, ví dụ:
    // List<Product> findByNameContaining(String keyword);
}

package com.example.ecommerce.impl;

import com.example.ecommerce.dto.ProductRequest;
import com.example.ecommerce.dto.ProductResponse;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.service.ProductService;
import org.springframework.stereotype.Service;
import com.example.ecommerce.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<ProductResponse> getAll() {
        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm với id = " + id));
        return toResponse(product);
    }

    @Override
    public ProductResponse create(ProductRequest request) {
        Product p = new Product();
        p.setName(request.getName());
        p.setDescription(request.getDescription());
        p.setPrice(request.getPrice());
        p.setStock(request.getStock());
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(null);

        Product saved = productRepository.save(p);
        return toResponse(saved);
    }

    @Override
    public ProductResponse update(Long id, ProductRequest request) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm với id = " + id));

        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setPrice(request.getPrice());
        existing.setStock(request.getStock());
        existing.setUpdatedAt(LocalDateTime.now());

        Product saved = productRepository.save(existing);
        return toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new NotFoundException("Không tìm thấy sản phẩm với id = " + id);
        }
        productRepository.deleteById(id);
    }

    // mapping Entity -> Response DTO
    private ProductResponse toResponse(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getStock(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}

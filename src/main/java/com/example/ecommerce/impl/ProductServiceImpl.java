package com.example.ecommerce.impl;

import com.example.ecommerce.dto.PageResponse;
import com.example.ecommerce.dto.ProductRequest;
import com.example.ecommerce.dto.ProductResponse;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.service.ProductService;
import com.example.ecommerce.spec.ProductSpecification;
import org.springframework.stereotype.Service;
import com.example.ecommerce.exception.NotFoundException;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
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

    @Override
    public ProductResponse updateImageUrl(Long id, String imageUrl) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm với id = " + id));

        product.setImageUrl(imageUrl);
        product.setUpdatedAt(LocalDateTime.now());

        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

    @Override
    public PageResponse<ProductResponse> search(String keyword,
                                                BigDecimal minPrice,
                                                BigDecimal maxPrice,
                                                int page, int size,
                                                String sort) {

        Sort sortObj = Sort.by("id").descending();
        if (sort != null && !sort.isBlank()) {
            // sort format: field,dir  (vd: price,asc)
            String[] parts = sort.split(",");
            if (parts.length == 2) {
                String field = parts[0].trim();
                String dir = parts[1].trim().toLowerCase();
                sortObj = "asc".equals(dir) ? Sort.by(field).ascending() : Sort.by(field).descending();
            }
        }

        Pageable pageable = PageRequest.of(page, size, sortObj);

        var spec = ProductSpecification.keyword(keyword)
                .and(ProductSpecification.minPrice(minPrice))
                .and(ProductSpecification.maxPrice(maxPrice));

        Page<Product> result = productRepository.findAll(spec, pageable);

        var items = result.getContent().stream().map(this::toResponse).toList();

        return new PageResponse<>(
                items,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
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
                p.getUpdatedAt(),
                p.getImageUrl()
        );
    }
}

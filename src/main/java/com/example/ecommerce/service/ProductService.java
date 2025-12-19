package com.example.ecommerce.service;

import com.example.ecommerce.dto.PageResponse;
import com.example.ecommerce.dto.ProductRequest;
import com.example.ecommerce.dto.ProductResponse;

import java.util.List;

public interface ProductService {

    List<ProductResponse> getAll();

    ProductResponse getById(Long id);

    ProductResponse create(ProductRequest request);

    ProductResponse update(Long id, ProductRequest request);

    void delete(Long id);

    ProductResponse updateImageUrl(Long id, String imageUrl);

    PageResponse<ProductResponse> search(String keyword,
                                         java.math.BigDecimal minPrice,
                                         java.math.BigDecimal maxPrice,
                                         int page, int size,
                                         String sort);
}

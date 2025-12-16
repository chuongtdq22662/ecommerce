package com.example.ecommerce.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String storeProductImage(Long productId, MultipartFile file);
}
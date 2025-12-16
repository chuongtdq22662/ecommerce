package com.example.ecommerce.impl;

import com.example.ecommerce.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path rootLocation = Paths.get("uploads/products");

    @Override
    public String storeProductImage(Long productId, MultipartFile file) {

        try {
            if (file.isEmpty()) {
                throw new RuntimeException("File rỗng");
            }

            // Tạo folder nếu chưa tồn tại
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation);
            }

            // Tên file: productId-timestamp-originalName để tránh trùng
            String originalName = file.getOriginalFilename();
            String filename = productId + "_" + System.currentTimeMillis() + "_" + originalName;

            Path destinationFile = rootLocation.resolve(filename).normalize().toAbsolutePath();

            // Lưu file
            file.transferTo(destinationFile.toFile());

            // Giá trị này sẽ lưu vào DB: có thể là path tương đối
            return "/uploads/products/" + filename;

        } catch (IOException e) {
            throw new RuntimeException("Không lưu được file: " + e.getMessage(), e);
        }
    }
}
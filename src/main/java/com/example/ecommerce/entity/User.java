package com.example.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false)
    private String password; // đã được mã hóa (BCrypt)

    @Column(nullable = false)
    private String role; // ví dụ: ROLE_USER, ROLE_ADMIN

    @Column(nullable = false)
    private boolean enabled = true;
}

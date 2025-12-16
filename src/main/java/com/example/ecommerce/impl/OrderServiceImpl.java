package com.example.ecommerce.impl;

import com.example.ecommerce.dto.CreateOrderRequest;
import com.example.ecommerce.dto.OrderItemRequest;
import com.example.ecommerce.dto.OrderItemResponse;
import com.example.ecommerce.dto.OrderResponse;
import com.example.ecommerce.entity.*;
import com.example.ecommerce.event.OrderCreatedEvent;
import com.example.ecommerce.event.OrderEventProducer;
import com.example.ecommerce.exception.NotFoundException;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.security.CustomUserDetails;
import com.example.ecommerce.service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderEventProducer producer;

    public OrderServiceImpl(OrderRepository orderRepository,
                            ProductRepository productRepository,
                            UserRepository userRepository, OrderEventProducer producer) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.producer = producer;
    }

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {

        User currentUser = getCurrentUser();

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        // duyệt từng item client gửi lên
        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new NotFoundException(
                            "Không tìm thấy sản phẩm với id = " + itemReq.getProductId()
                    ));

            if (product.getStock() < itemReq.getQuantity()) {
                throw new IllegalArgumentException(
                        "Sản phẩm " + product.getName() + " không đủ tồn kho"
                );
            }

            // trừ tồn kho
            product.setStock(product.getStock() - itemReq.getQuantity());
            productRepository.save(product);

            BigDecimal price = product.getPrice();
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .price(price)
                    .subtotal(subtotal)
                    .build();

            orderItems.add(orderItem);
            total = total.add(subtotal);
        }

        // tạo order
        Order order = new Order();
        order.setUser(currentUser);
        order.setTotalAmount(total);
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());
        order.setItems(orderItems);

        // set order cho từng item (vì mappedBy)
        orderItems.forEach(oi -> oi.setOrder(order));

        Order saved = orderRepository.save(order);

        // publish event gửi mail
        OrderCreatedEvent event = new OrderCreatedEvent(
                saved.getId(),
                currentUser.getUsername(),
                currentUser.getEmail(),
                saved.getTotalAmount()
        );
        producer.publishOrderCreated(event);

        return toResponse(saved);
    }

    @Override
    public OrderResponse getOrderById(Long id) {
        User currentUser = getCurrentUser();

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy order với id = " + id));

        // đảm bảo user chỉ xem được đơn của chính mình
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new NotFoundException("Order không thuộc về bạn");
        }

        return toResponse(order);
    }

    @Override
    public List<OrderResponse> getMyOrders() {
        User currentUser = getCurrentUser();
        return orderRepository.findByUserOrderByCreatedAtDesc(currentUser)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // helper: lấy user hiện tại từ SecurityContext
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
        String username = cud.getUsername();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User không tồn tại"));
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems()
                .stream()
                .map(oi -> new OrderItemResponse(
                        oi.getProduct().getId(),
                        oi.getProduct().getName(),
                        oi.getQuantity(),
                        oi.getPrice(),
                        oi.getSubtotal()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                itemResponses
        );
    }
}

package com.example.ecommerce.event;

import com.example.ecommerce.config.RabbitConfig;
import com.example.ecommerce.service.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class OrderEmailConsumer {

    private final EmailService emailService;

    public OrderEmailConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = RabbitConfig.ORDER_EMAIL_QUEUE)
    public void handle(OrderCreatedEvent event) throws Exception {
        String html = """
                <h2>Đặt hàng thành công!</h2>
                <p>Xin chào <b>%s</b>,</p>
                <p>Đơn hàng <b>#%d</b> đã được tạo.</p>
                <p>Tổng tiền: <b>%s</b></p>
                <p>Cảm ơn bạn!</p>
                """.formatted(event.username(), event.orderId(), event.totalAmount());

        emailService.sendHtml(event.email(), "Xác nhận đơn hàng #" + event.orderId(), html);
    }
}

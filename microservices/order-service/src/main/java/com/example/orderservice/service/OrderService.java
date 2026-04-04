package com.example.orderservice.service;

import com.example.orderservice.client.ProductClient;
import com.example.orderservice.client.UserClient;
import com.example.orderservice.dto.ProductDTO;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserClient userClient;

    @Autowired
    private ProductClient productClient;

    @Transactional
    public Order placeOrder(Long userId, List<OrderItem> items) {
        // 1. Kiểm tra User
        userClient.getUserById(userId);

        Double totalAmount = 0.0;
        Order order = new Order();
        order.setUserId(userId);

        // Track các items đã reduce stock thành công để compensation nếu cần
        List<Long> compensatedProductIds = new ArrayList<>();
        List<Integer> compensatedQuantities = new ArrayList<>();

        try {
            for (OrderItem item : items) {
                // 2. Lấy giá hiện tại từ Product Service
                ProductDTO product = productClient.getProductById(item.getProductId());
                item.setPrice(product.getPrice());

                // 3. Trừ tồn kho
                productClient.reduceStock(item.getProductId(), item.getQuantity());
                compensatedProductIds.add(item.getProductId());
                compensatedQuantities.add(item.getQuantity());

                totalAmount += item.getPrice() * item.getQuantity();
                order.addItem(item);
            }

            order.setTotalAmount(totalAmount);
            return orderRepository.save(order);

        } catch (Exception e) {
            // Compensation: hoàn lại stock đã trừ trước đó
            for (int i = 0; i < compensatedProductIds.size(); i++) {
                try {
                    productClient.increaseStock(compensatedProductIds.get(i), compensatedQuantities.get(i));
                    log.warn("Compensated stock for product {}", compensatedProductIds.get(i));
                } catch (Exception compensateEx) {
                    log.error("CRITICAL: Failed to compensate stock for product {}. Manual intervention required.",
                            compensatedProductIds.get(i), compensateEx);
                }
            }
            throw e; // re-throw để @Transactional rollback DB
        }
    }

    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }
}

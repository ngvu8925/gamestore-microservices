package com.example.orderservice.service;

import com.example.orderservice.client.ProductClient;
import com.example.orderservice.client.UserClient;
import com.example.orderservice.dto.ProductDTO;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserClient userClient;

    @Autowired
    private ProductClient productClient;

    @Transactional
    public Order placeOrder(Long userId, List<OrderItem> items) {
        // 1. Kiểm tra User (Ngoại lệ sẽ văng ra nếu User không có thật)
        userClient.getUserById(userId);

        Double totalAmount = 0.0;
        Order order = new Order();
        order.setUserId(userId);

        for (OrderItem item : items) {
            // 2. Lấy giá hiện tại từ Product Service
            ProductDTO product = productClient.getProductById(item.getProductId());
            item.setPrice(product.getPrice());
            
            // 3. Trừ tồn kho từ Product Service
            productClient.reduceStock(item.getProductId(), item.getQuantity());

            totalAmount += item.getPrice() * item.getQuantity();
            order.addItem(item);
        }

        order.setTotalAmount(totalAmount);
        return orderRepository.save(order);
    }

    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }
}

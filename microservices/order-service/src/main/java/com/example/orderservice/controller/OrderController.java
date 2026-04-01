package com.example.orderservice.controller;

import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * TÃ¡ÂºÂ O Ã„Â Ã†Â N HÃƒâ‚¬NG
     * POST /api/orders
     */
    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody OrderRequest request) {
        try {
            List<OrderItem> items = request.getItems().stream()
                    .map(entry -> new OrderItem(entry.getProductId(), entry.getQuantity(), 0.0))
                    .collect(Collectors.toList());

            Order order = orderService.placeOrder(request.getUserId(), items);
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    /**
     * LÃ¡ÂºÂ¤Y LÃ¡Â»ÂŠCH SÃ¡Â»Â¬ Ã„Â Ã†Â N HÃƒâ‚¬NG CÃ¡Â»Â¦A USER
     * GET /api/orders/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }
}

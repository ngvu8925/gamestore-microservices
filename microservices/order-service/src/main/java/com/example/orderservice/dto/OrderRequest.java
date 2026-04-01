package com.example.orderservice.dto;

import java.util.List;

public class OrderRequest {
    private Long userId;
    private List<OrderItemEntry> items;

    public static class OrderItemEntry {
        private Long productId;
        private Integer quantity;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public List<OrderItemEntry> getItems() { return items; }
    public void setItems(List<OrderItemEntry> items) { this.items = items; }
}

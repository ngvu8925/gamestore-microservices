package com.example.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class OrderRequest {

    @NotNull(message = "userId không được null")
    private Long userId;

    @NotEmpty(message = "Danh sách items không được rỗng")
    private List<@Valid OrderItemEntry> items;

    public static class OrderItemEntry {

        @NotNull(message = "productId không được null")
        private Long productId;

        @NotNull(message = "quantity không được null")
        @Min(value = 1, message = "Quantity phải >= 1")
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

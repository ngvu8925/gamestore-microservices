package com.example.orderservice.client.fallback;

import com.example.orderservice.client.UserClient;
import org.springframework.stereotype.Component;

@Component
public class UserClientFallback implements UserClient {

    @Override
    public Object getUserById(Long id) {
        throw new RuntimeException("User service tạm thời không khả dụng. Vui lòng thử lại sau.");
    }
}

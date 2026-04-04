package com.example.gatewayservice;

import com.example.gatewayservice.filter.AuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bug Condition Exploration Test - Gateway Error Body (Test 1.6)
 *
 * Sends a request with an invalid token and asserts the response body is not empty.
 * EXPECTED TO FAIL: AuthenticationFilter.onError() currently calls response.setComplete()
 * without writing any body, so the response body is always empty on error.
 *
 * Validates: Requirements 3.3
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
class BugConditionGatewayTest {

    @Autowired
    AuthenticationFilter authenticationFilter;

    /**
     * isBugCondition_EmptyErrorBody
     *
     * Invokes the AuthenticationFilter with an invalid token and asserts
     * that the response body is NOT empty (should contain a JSON error message).
     *
     * EXPECTED TO FAIL: onError() uses response.setComplete() which writes no body.
     *
     * Validates: Requirements 3.3
     */
    @Test
    @DisplayName("isBugCondition_EmptyErrorBody - Invalid token must return 401 with non-empty JSON error body")
    void isBugCondition_EmptyErrorBody() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/products/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer this.is.an.invalid.token")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AuthenticationFilter.Config config = new AuthenticationFilter.Config();
        GatewayFilter filter = authenticationFilter.apply(config);

        // Execute the filter; chain is a no-op since we expect early rejection
        filter.filter(exchange, ex -> Mono.empty()).block();

        // Assert HTTP 401
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode(),
                "Expected 401 UNAUTHORIZED for invalid token");

        // Assert response body is NOT empty
        // COUNTEREXAMPLE: body will be empty because onError() calls response.setComplete()
        // without writing any content.
        String body = exchange.getResponse().getBodyAsString().block();
        assertNotNull(body,
                "COUNTEREXAMPLE - Response body is NULL for invalid token request.\n"
                        + "Expected a JSON error body like {\"error\": \"Token đã hết hạn hoặc không hợp lệ!\"}.\n"
                        + "Root cause: AuthenticationFilter.onError() calls response.setComplete() "
                        + "without writing any body content.");
        assertFalse(body.isEmpty(),
                "COUNTEREXAMPLE - Response body is EMPTY for invalid token request.\n"
                        + "Expected a JSON error body like {\"error\": \"Token đã hết hạn hoặc không hợp lệ!\"}.\n"
                        + "Root cause: AuthenticationFilter.onError() calls response.setComplete() "
                        + "without writing any body content.");
    }

    /**
     * isBugCondition_EmptyErrorBody (missing token variant)
     *
     * Sends a request with NO Authorization header and asserts response body is not empty.
     * EXPECTED TO FAIL for the same reason as above.
     *
     * Validates: Requirements 3.3
     */
    @Test
    @DisplayName("isBugCondition_EmptyErrorBody - Missing token must return 401 with non-empty JSON error body")
    void isBugCondition_EmptyErrorBody_MissingToken() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/products/1")
                .build(); // No Authorization header

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AuthenticationFilter.Config config = new AuthenticationFilter.Config();
        GatewayFilter filter = authenticationFilter.apply(config);

        filter.filter(exchange, ex -> Mono.empty()).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode(),
                "Expected 401 UNAUTHORIZED for missing token");

        String body = exchange.getResponse().getBodyAsString().block();
        assertNotNull(body,
                "COUNTEREXAMPLE - Response body is NULL when Authorization header is missing.\n"
                        + "Expected a JSON error body like {\"error\": \"Thiếu Token bảo mật!\"}.");
        assertFalse(body.isEmpty(),
                "COUNTEREXAMPLE - Response body is EMPTY when Authorization header is missing.\n"
                        + "Expected a JSON error body like {\"error\": \"Thiếu Token bảo mật!\"}.");
    }
}

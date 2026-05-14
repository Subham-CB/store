package com.example.store.integration;

import com.example.store.api.model.ErrorResponseDTO;
import com.example.store.api.model.OrderDTO;
import com.example.store.api.model.OrderProductDTO;
import com.example.store.api.model.OrderRequestDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private CacheManager cacheManager;

    // Seeded data facts from data.sql and product_data.sql
    private static final Long   SEEDED_ORDER_ID          = 1L;
    private static final String SEEDED_ORDER_DESCRIPTION = "Handcrafted Soft Chair";
    private static final Long   SEEDED_CUSTOMER_ID       = 6L;
    private static final String SEEDED_CUSTOMER_NAME     = "Vicki Kutch";

    // Valid seeded IDs to use when creating an order
    private static final Long   CREATE_CUSTOMER_ID       = 1L;   // Muriel Donnelly
    private static final Set<Long> CREATE_PRODUCT_IDS   = Set.of(1L, 2L); // Ergonomic Steel Keyboard, Rustic Wooden Chair

    @BeforeEach
    void evictCache() {
        Cache cache = cacheManager.getCache("orders");
        if (cache != null) {
            cache.clear();
        }
    }

    @Test
    @DisplayName("POST /order - creates a new order with valid customer and products, returns 201")
    void createOrder_success_returns201() {

        OrderRequestDTO request = new OrderRequestDTO();
        request.setDescription("Integration Test Order");
        request.setCustomerId(CREATE_CUSTOMER_ID);
        request.setProductIds(CREATE_PRODUCT_IDS);

        ResponseEntity<OrderDTO> response =
                testRestTemplate.postForEntity("/order", request, OrderDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        OrderDTO body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getId()).isPositive();
        assertThat(body.getDescription()).isEqualTo("Integration Test Order");


        assertThat(body.getCustomer()).isNotNull();
        assertThat(body.getCustomer().getId()).isEqualTo(CREATE_CUSTOMER_ID);
        assertThat(body.getCustomer().getName()).isEqualTo("Muriel Donnelly");


        assertThat(body.getProducts()).isNotNull();
        assertThat(body.getProducts())
                .extracting(OrderProductDTO::getId)
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("POST /order - returns 400 when description is missing")
    void createOrder_missingDescription_returns400() {
        OrderRequestDTO request = new OrderRequestDTO();
        // description not set
        request.setCustomerId(CREATE_CUSTOMER_ID);
        request.setProductIds(CREATE_PRODUCT_IDS);

        ResponseEntity<ErrorResponseDTO> response =
                testRestTemplate.postForEntity("/order", request, ErrorResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getValidationErrors()).containsKey("description");
    }

    @Test
    @DisplayName("POST /order - returns 400 when productIds is missing")
    void createOrder_missingProductIds_returns400() {
        OrderRequestDTO request = new OrderRequestDTO();
        request.setDescription("Missing Products Order");
        request.setCustomerId(CREATE_CUSTOMER_ID);

        ResponseEntity<ErrorResponseDTO> response =
                testRestTemplate.postForEntity("/order", request, ErrorResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getValidationErrors()).containsKey("productIds");
    }

    @Test
    @DisplayName("POST /order - returns 404 when customer does not exist")
    void createOrder_nonExistentCustomer_returns404() {
        OrderRequestDTO request = new OrderRequestDTO();
        request.setDescription("Ghost Customer Order");
        request.setCustomerId(999999L);
        request.setProductIds(CREATE_PRODUCT_IDS);

        ResponseEntity<ErrorResponseDTO> response =
                testRestTemplate.postForEntity("/order", request, ErrorResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertNotNull(response.getBody());
        assertThat(response.getBody().getMessage()).contains("999999");
    }

    @Test
    @DisplayName("POST /order - returns 404 when one or more product IDs do not exist")
    void createOrder_nonExistentProduct_returns404() {
        OrderRequestDTO request = new OrderRequestDTO();
        request.setDescription("Ghost Product Order");
        request.setCustomerId(CREATE_CUSTOMER_ID);
        request.setProductIds(Set.of(1L, 999999L)); // 999999 does not exist

        ResponseEntity<ErrorResponseDTO> response =
                testRestTemplate.postForEntity("/order", request, ErrorResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertNotNull(response.getBody());
        assertThat(response.getBody().getMessage()).contains("999999");
    }



    @Test
    @DisplayName("GET /order - returns first page of 30 orders")
    void getOrders_returnsDefaultPageOf30() {
        ResponseEntity<OrderDTO[]> response =
                testRestTemplate.getForEntity("/order", OrderDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(30);
        assertThat(response.getBody())
                .allSatisfy(order -> {
                    assertThat(order.getId()).isPositive();
                    assertThat(order.getDescription()).isNotBlank();
                    assertThat(order.getCustomer()).isNotNull();
                    assertThat(order.getCustomer().getId()).isPositive();
                });
    }


    @Test
    @DisplayName("GET /order/1 - returns seeded order with correct description and customer")
    void getOrderById_seededId_returnsCorrectOrder() {
        ResponseEntity<OrderDTO> response =
                testRestTemplate.getForEntity("/order/" + SEEDED_ORDER_ID, OrderDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        OrderDTO body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getId()).isEqualTo(SEEDED_ORDER_ID);
        assertThat(body.getDescription()).isEqualTo(SEEDED_ORDER_DESCRIPTION);


        assertThat(body.getCustomer()).isNotNull();
        assertThat(body.getCustomer().getId()).isEqualTo(SEEDED_CUSTOMER_ID);
        assertThat(body.getCustomer().getName()).isEqualTo(SEEDED_CUSTOMER_NAME);


        assertThat(body.getProducts()).isNotNull();
        assertThat(body.getProducts())
                .extracting(OrderProductDTO::getId)
                .containsExactlyInAnyOrder(1L, 5L);
    }

    @Test
    @DisplayName("GET /order/999999 - returns 404 for non-existent order id")
    void getOrderById_nonExistentId_returns404() {
        ResponseEntity<ErrorResponseDTO> response =
                testRestTemplate.getForEntity("/order/999999", ErrorResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ErrorResponseDTO error = response.getBody();
        assertThat(error).isNotNull();
        assertThat(error.getStatus()).isEqualTo(404);
        assertThat(error.getMessage()).contains("999999");
    }

    @Test
    @DisplayName("Redis - GET /order/{id} populates cache on first call, eviction clears it")
    void getOrderById_cachingAndEviction() {
        Cache cache = cacheManager.getCache("orders");
        assertThat(cache).isNotNull();

        String cacheKey = "Id_" + SEEDED_ORDER_ID;

        assertThat(cache.get(cacheKey)).isNull();

        ResponseEntity<OrderDTO> firstResponse =
                testRestTemplate.getForEntity("/order/" + SEEDED_ORDER_ID, OrderDTO.class);
        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(cache.get(cacheKey)).isNotNull();

        ResponseEntity<OrderDTO> secondResponse =
                testRestTemplate.getForEntity("/order/" + SEEDED_ORDER_ID, OrderDTO.class);
        assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertNotNull(secondResponse.getBody());
        Assertions.assertNotNull(firstResponse.getBody());
        assertThat(secondResponse.getBody().getDescription())
                .isEqualTo(firstResponse.getBody().getDescription());

        cache.evict(cacheKey);
        assertThat(cache.get(cacheKey)).isNull();
    }

    @Test
    @DisplayName("POST /order - creating an order evicts the orders cache")
    void createOrder_evictsOrdersCache() {
        Cache cache = cacheManager.getCache("orders");
        assertThat(cache).isNotNull();

        testRestTemplate.getForEntity("/order/" + SEEDED_ORDER_ID, OrderDTO.class);
        String cacheKey = "Id_" + SEEDED_ORDER_ID;
        assertThat(cache.get(cacheKey)).isNotNull();

        OrderRequestDTO request = new OrderRequestDTO();
        request.setDescription("Cache Eviction Test Order");
        request.setCustomerId(CREATE_CUSTOMER_ID);
        request.setProductIds(CREATE_PRODUCT_IDS);
        testRestTemplate.postForEntity("/order", request, OrderDTO.class);

        assertThat(cache.get(cacheKey)).isNull();
    }
}

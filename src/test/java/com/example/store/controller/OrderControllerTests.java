package com.example.store.controller;

import com.example.store.api.model.OrderCustomerDTO;
import com.example.store.api.model.OrderDTO;
import com.example.store.api.model.OrderRequestDTO;
import com.example.store.component.GlobalSearchDefaults;
import com.example.store.exception.CustomerNotFoundException;
import com.example.store.exception.OrderNotFoundException;
import com.example.store.exception.ProductNotFoundException;
import com.example.store.mapper.CustomerMapper;
import com.example.store.service.OrderService;
import com.example.store.util.PageableBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@ComponentScan(basePackageClasses = CustomerMapper.class)
@RequiredArgsConstructor
class OrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private GlobalSearchDefaults globalSearchProp;

    @MockitoBean
    private PageableBuilder pageableBuilder;

    private OrderDTO orderDTO;
    private OrderRequestDTO orderRequestDTO;
    private Pageable mockPageable;

    @BeforeEach
    void setUp() {
        OrderCustomerDTO orderCustomerDTO = new OrderCustomerDTO();
        orderCustomerDTO.setId(1L);
        orderCustomerDTO.setName("John Doe");

        orderDTO = new OrderDTO();
        orderDTO.setId(1L);
        orderDTO.setDescription("Test Order");
        orderDTO.setCustomer(orderCustomerDTO);

        orderRequestDTO = new OrderRequestDTO();
        orderRequestDTO.setDescription("Test Order");
        orderRequestDTO.setCustomerId(1L);
        orderRequestDTO.setProductIds(Set.of(1L, 2L));

        mockPageable = PageRequest.of(0, 30);

        when(globalSearchProp.getLimit()).thenReturn(30);
        when(globalSearchProp.getSortField()).thenReturn("id");
        when(globalSearchProp.getDirection()).thenReturn("asc");

        when(pageableBuilder.buildPageable(any(), any(), any(), any(), anyInt(), anyString(), anyString()))
                .thenReturn(mockPageable);
    }

    @Test
    @DisplayName("POST /order - Should return 201 with created order body")
    void createOrder_validRequest_returns201() throws Exception {

        when(orderService.createOrder(orderRequestDTO)).thenReturn(orderDTO);

        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Test Order"))
                .andExpect(jsonPath("$.customer.id").value(1))
                .andExpect(jsonPath("$.customer.name").value("John Doe"));
    }

    @Test
    @DisplayName("POST /order - returns 400 when description is missing")
    void createOrder_missingDescription_returns400() throws Exception {
        OrderRequestDTO request = new OrderRequestDTO();
        request.setCustomerId(1L);
        request.setProductIds(Set.of(1L));

        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.description").exists());
    }

    @Test
    @DisplayName("POST /order - returns 400 when customerId is missing")
    void createOrder_missingCustomerId_returns400() throws Exception {
        OrderRequestDTO request = new OrderRequestDTO();
        request.setDescription("Test Order");
        request.setProductIds(Set.of(1L));

        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.customerId").exists());
    }

    @Test
    @DisplayName("POST /order - returns 400 when productIds is missing")
    void createOrder_missingProductIds_returns400() throws Exception {
        OrderRequestDTO request = new OrderRequestDTO();
        request.setDescription("Test Order");
        request.setCustomerId(1L);

        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.productIds").exists());
    }

    @Test
    @DisplayName("GET /customer - Find all")
    void testGetOrder() throws Exception {
        when(orderService.findAllOrders(mockPageable)).thenReturn(List.of(orderDTO));

        mockMvc.perform(get("/order"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("Test Order"))
                .andExpect(jsonPath("$[0].customer.id").value(1))
                .andExpect(jsonPath("$[0].customer.name").value("John Doe"));
    }

    @Test
    @DisplayName("POST /order - returns 404 when customer does not exist")
    void createOrder_nonExistentCustomer_returns404() throws Exception {
        when(orderService.createOrder(any())).thenThrow(new CustomerNotFoundException(999L));

        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Customer not found with Id : 999"));
    }

    @Test
    @DisplayName("POST /order - returns 404 when product does not exist")
    void createOrder_nonExistentProduct_returns404() throws Exception {
        when(orderService.createOrder(any())).thenThrow(new ProductNotFoundException(Set.of(999L)));

        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product not found with Id : [999]"));
    }

    @Test
    @DisplayName("GET /order/{id} - Find by id")
    void getOrders_returnsList() throws Exception {
        Long orderId = 1L;
        when(orderService.findOrderById(orderId)).thenReturn(orderDTO);

        mockMvc.perform(get("/order/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Test Order"))
                .andExpect(jsonPath("$.customer.id").value(1))
                .andExpect(jsonPath("$.customer.name").value("John Doe"));
    }

    @Test
    @DisplayName("GET /order - returns 200 with empty array when no orders exist")
    void getOrders_noOrders_returnsEmptyList() throws Exception {
        when(orderService.findAllOrders(mockPageable)).thenReturn(List.of());

        mockMvc.perform(get("/order"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /order?sortDir=INVALID - returns 400 for invalid sort direction enum")
    void getOrders_invalidSortDir_returns400() throws Exception {
        mockMvc.perform(get("/order").param("sortDir", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("GET /order/{id} - returns 200 with correct order body")
    void getOrderById_existingId_returns200() throws Exception {
        when(orderService.findOrderById(1L)).thenReturn(orderDTO);

        mockMvc.perform(get("/order/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Test Order"))
                .andExpect(jsonPath("$.customer.id").value(1));
    }

    @Test
    @DisplayName("GET /order/{id} - returns 404 when order does not exist")
    void getOrderById_nonExistentId_returns404() throws Exception {
        when(orderService.findOrderById(999L)).thenThrow(new OrderNotFoundException(999L));

        mockMvc.perform(get("/order/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Order not found with Id: 999"));
    }
}

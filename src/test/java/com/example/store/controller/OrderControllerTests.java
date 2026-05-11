package com.example.store.controller;

import com.example.store.component.GlobalSearchProp;
import com.example.store.dto.order.OrderCustomerDTO;
import com.example.store.dto.order.OrderDTO;
import com.example.store.dto.order.OrderRequestDTO;
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
    private GlobalSearchProp globalSearchProp;

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

        mockPageable = PageRequest.of(0, 10);

        when(globalSearchProp.getLimit()).thenReturn(20);
        when(globalSearchProp.getSortField()).thenReturn("id");
        when(globalSearchProp.getDirection()).thenReturn("asc");

        when(pageableBuilder.buildPageable(any(), any(), any(), any(), anyInt(), anyString(), anyString()))
                .thenReturn(mockPageable);
    }

    @Test
    @DisplayName("POST /order - Should return 201 Created")
    void testCreateOrder() throws Exception {

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
    @DisplayName("GET /order/{id} - Find by id")
    void testGetOrderById() throws Exception {
        Long orderId = 1L;
        when(orderService.findOrderById(orderId)).thenReturn(orderDTO);

        mockMvc.perform(get("/order/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Test Order"))
                .andExpect(jsonPath("$.customer.id").value(1))
                .andExpect(jsonPath("$.customer.name").value("John Doe"));
    }
}

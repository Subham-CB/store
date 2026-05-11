package com.example.store.service;

import com.example.store.dto.order.OrderDTO;
import com.example.store.dto.order.OrderRequestDTO;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {

    List<OrderDTO> findAllOrders(Pageable pageable);

    OrderDTO createOrder(OrderRequestDTO orderRequestDTO);

    OrderDTO findOrderById(Long orderId);

    void clearOrdersCache();
}

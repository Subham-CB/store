package com.example.store.service;

import com.example.store.dto.OrderDTO;
import com.example.store.dto.OrderRequestDTO;

import java.util.List;

public interface OrderService {

    List<OrderDTO> findAllOrders();

    OrderDTO createOrder(OrderRequestDTO orderRequestDTO);
}

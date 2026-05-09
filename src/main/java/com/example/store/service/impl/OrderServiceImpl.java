package com.example.store.service.impl;

import com.example.store.dto.OrderDTO;
import com.example.store.dto.OrderRequestDTO;
import com.example.store.entity.Customer;
import com.example.store.entity.Order;
import com.example.store.exception.CustomerNotFoundException;
import com.example.store.exception.OrderNotFoundException;
import com.example.store.mapper.OrderMapper;
import com.example.store.repository.CustomerRepository;
import com.example.store.repository.OrderRepository;
import com.example.store.service.OrderService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final OrderMapper orderMapper;

    @Override
    public List<OrderDTO> findAllOrders(Pageable pageable) {
        return orderMapper.ordersToOrderDTOs(orderRepository.findAll(pageable).getContent());
    }

    @Override
    public OrderDTO createOrder(OrderRequestDTO orderRequestDTO) {

        Order order = orderMapper.orderRequestDTOToOrder(orderRequestDTO);

        Customer customer = customerRepository
                .findById(orderRequestDTO.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(orderRequestDTO.getCustomerId()));

        order.setCustomer(customer);

        return orderMapper.orderToOrderDTO(orderRepository.save(order));
    }

    @Override
    public OrderDTO findOrderById(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(()->new OrderNotFoundException(orderId));

        return orderMapper.orderToOrderDTO(order);
    }
}

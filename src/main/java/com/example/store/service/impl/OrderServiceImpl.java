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

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final OrderMapper orderMapper;

    @Cacheable(
            value = "orders",
            key = "'all_page_' + #pageable.pageNumber + '_size_' + #pageable.pageSize + '_sort_' + #pageable.sort"
    )
    @Override
    public List<OrderDTO> findAllOrders(final Pageable pageable) {
        return orderMapper.ordersToOrderDTOs(orderRepository.findAll(pageable).getContent());
    }

    @CacheEvict(
            value = "orders",
            allEntries = true
    )
    @Override
    @Transactional
    public OrderDTO createOrder(final OrderRequestDTO orderRequestDTO) {

        Customer customer = customerRepository
                .findById(orderRequestDTO.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(orderRequestDTO.getCustomerId()));

        final Order order = orderMapper.orderRequestDTOToOrder(orderRequestDTO);
        order.setCustomer(customer);

        return orderMapper.orderToOrderDTO(orderRepository.save(order));
    }

    @Cacheable(
            value = "orders",
            key = "'Id_' + #orderId"
    )
    @Override
    public OrderDTO findOrderById(final Long orderId) {

        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));

        return orderMapper.orderToOrderDTO(order);
    }

    @CacheEvict(value = "orders",allEntries = true)
    @Override
    public void clearOrdersCache() {
    }
}

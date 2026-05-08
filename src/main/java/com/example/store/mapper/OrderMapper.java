package com.example.store.mapper;

import com.example.store.dto.OrderDTO;
import com.example.store.dto.OrderRequestDTO;
import com.example.store.entity.Order;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "customer", source = "customer")
    OrderDTO orderToOrderDTO(Order order);

    List<OrderDTO> ordersToOrderDTOs(List<Order> orders);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    Order orderRequestDTOToOrder(OrderRequestDTO orderRequestDTO);
}

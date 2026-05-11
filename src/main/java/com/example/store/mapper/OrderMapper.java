package com.example.store.mapper;

import com.example.store.dto.OrderDTO;
import com.example.store.dto.OrderRequestDTO;
import com.example.store.entity.Order;

import com.example.store.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "customer", source = "customer")
    @Mapping(target = "productIds", source = "products")
    OrderDTO orderToOrderDTO(Order order);

    List<OrderDTO> ordersToOrderDTOs(List<Order> orders);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "products", ignore = true)
    Order orderRequestDTOToOrder(OrderRequestDTO orderRequestDTO);

    default Set<Long> map(Set<Product> products){
        return products.stream()
                .map(Product::getId)
                .collect(Collectors.toSet());
    }
}

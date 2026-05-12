package com.example.store.mapper;

import com.example.store.api.model.CustomerDTO;
import com.example.store.api.model.CustomerOrderDTO;
import com.example.store.api.model.CustomerRequestDTO;
import com.example.store.api.model.OrderCustomerDTO;
import com.example.store.entity.Customer;
import com.example.store.entity.Order;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerDTO customerToCustomerDTO(Customer customer);

    OrderCustomerDTO customerToOrderCustomerDTO(Customer customer);

    List<CustomerDTO> customersToCustomerDTOs(List<Customer> customer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orders", ignore = true)
    Customer customerRequestDTOToCustomer(CustomerRequestDTO customerRequestDTO);

    CustomerOrderDTO orderToCustomerOrderDTO(Order order);
}

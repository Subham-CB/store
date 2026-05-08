package com.example.store.service;

import com.example.store.dto.CustomerDTO;
import com.example.store.dto.CustomerRequestDTO;

import java.util.List;

public interface CustomerService {

    List<CustomerDTO> findAllCustomers();

    CustomerDTO createCustomer(CustomerRequestDTO customerRequestDTO);
}

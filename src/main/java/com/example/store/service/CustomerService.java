package com.example.store.service;

import com.example.store.dto.CustomerDTO;
import com.example.store.dto.CustomerRequestDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomerService {

    List<CustomerDTO> findCustomers(Pageable pageable);

    CustomerDTO createCustomer(CustomerRequestDTO customerRequestDTO);

    List<CustomerDTO> findCustomersNameContainingSubString(Pageable pageable, String name);
}

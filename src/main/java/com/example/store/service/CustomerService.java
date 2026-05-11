package com.example.store.service;

import com.example.store.dto.CustomerDTO;
import com.example.store.dto.CustomerRequestDTO;

import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomerService {

    List<CustomerDTO> findCustomers(Pageable pageable);

    CustomerDTO createCustomer(CustomerRequestDTO customerRequestDTO);

    List<CustomerDTO> findCustomersNameContainingSubString(Pageable pageable, String name);

    void clearCustomersCache();

    CustomerDTO findCustomerById(Long id);
}

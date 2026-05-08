package com.example.store.service.impl;

import com.example.store.dto.CustomerDTO;
import com.example.store.dto.CustomerRequestDTO;
import com.example.store.mapper.CustomerMapper;
import com.example.store.repository.CustomerRepository;
import com.example.store.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public List<CustomerDTO> findAllCustomers() {
        return customerMapper.customersToCustomerDTOs(customerRepository.findAll());
    }

    @Override
    public CustomerDTO createCustomer(CustomerRequestDTO customerRequestDTO) {
        return customerMapper.customerToCustomerDTO(customerRepository.save(customerMapper.customerRequestDTOToCustomer(customerRequestDTO)));
    }
}

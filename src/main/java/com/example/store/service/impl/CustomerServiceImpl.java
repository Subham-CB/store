package com.example.store.service.impl;

import com.example.store.dto.CustomerDTO;
import com.example.store.dto.CustomerRequestDTO;
import com.example.store.entity.Customer;
import com.example.store.mapper.CustomerMapper;
import com.example.store.repository.CustomerRepository;
import com.example.store.service.CustomerService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public List<CustomerDTO> findCustomers(Pageable pageable) {

        final Page<Customer> customerPage = customerRepository.findAll(pageable);
        return customerMapper.customersToCustomerDTOs(customerPage.getContent());
    }

    @Override
    public CustomerDTO createCustomer(CustomerRequestDTO customerRequestDTO) {

        Customer customer = customerMapper.customerRequestDTOToCustomer(customerRequestDTO);
        return customerMapper.customerToCustomerDTO(customerRepository.save(customer));
    }

    @Override
    public List<CustomerDTO> findCustomersNameContainingSubString(Pageable pageable, String name) {

        final List<Customer> customerPage = customerRepository.findCustomersByNameContainingIgnoreCase(pageable,name);
        return customerMapper.customersToCustomerDTOs(customerPage);
    }

}

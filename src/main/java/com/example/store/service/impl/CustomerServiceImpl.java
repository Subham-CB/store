package com.example.store.service.impl;

import com.example.store.api.model.CustomerDTO;
import com.example.store.api.model.CustomerRequestDTO;
import com.example.store.entity.Customer;
import com.example.store.exception.CustomerNotFoundException;
import com.example.store.mapper.CustomerMapper;
import com.example.store.repository.CustomerRepository;
import com.example.store.service.CustomerService;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Cacheable(
            value = "customers",
            key = "'all_page_' + #pageable.pageNumber + '_size_' + #pageable.pageSize + '_sort_' + #pageable.sort")
    @Override
    public List<CustomerDTO> findCustomers(final Pageable pageable) {

        final Page<Customer> customerPage = customerRepository.findAll(pageable);
        return customerMapper.customersToCustomerDTOs(customerPage.getContent());
    }

    @CacheEvict(value = "customers", allEntries = true)
    @Override
    @Transactional
    public CustomerDTO createCustomer(final CustomerRequestDTO customerRequestDTO) {

        Customer customer = customerMapper.customerRequestDTOToCustomer(customerRequestDTO);
        return customerMapper.customerToCustomerDTO(customerRepository.save(customer));
    }

    @Cacheable(
            value = "customers",
            key =
                    "'search_' + #name + '_page_' + #pageable.pageNumber + '_size_' + #pageable.pageSize + '_sort_' + #pageable.sort")
    @Override
    public List<CustomerDTO> findCustomersNameContainingSubString(Pageable pageable, final String name) {

        final List<Customer> customerPage = customerRepository.findCustomersByNameContainingIgnoreCase(pageable, name);
        return customerMapper.customersToCustomerDTOs(customerPage);
    }

    @CacheEvict(value = "customers", allEntries = true)
    @Override
    public void clearCustomersCache() {}

    @Cacheable(value = "customers", key = "'id_'+#id")
    @Override
    public CustomerDTO findCustomerById(final Long id) {
        Customer customer =
                customerRepository.findCustomerById(id).orElseThrow(() -> new CustomerNotFoundException(id));

        return customerMapper.customerToCustomerDTO(customer);
    }
}

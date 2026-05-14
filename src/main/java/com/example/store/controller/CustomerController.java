package com.example.store.controller;

import com.example.store.api.CustomerApi;
import com.example.store.api.model.CustomerDTO;
import com.example.store.api.model.CustomerRequestDTO;
import com.example.store.api.model.SortEnumDTO;
import com.example.store.component.CustomerSearchDefaults;
import com.example.store.service.CustomerService;
import com.example.store.util.PageableBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CustomerController implements CustomerApi {

    private final CustomerService customerService;
    private final CustomerSearchDefaults customerSearchDefaults;
    private final PageableBuilder pageableBuilder;

    @Override
    public ResponseEntity<List<CustomerDTO>> getCustomers(
            String name, Integer page, Integer limit, String sortBy, SortEnumDTO sortDir) {

        log.debug("GET /customers called with  page={}, limit={}, sortBy={}, sortDir={}", page, limit, sortBy, sortDir);

        final Pageable pageable = pageableBuilder.buildPageable(
                page,
                limit,
                sortBy,
                sortDir,
                customerSearchDefaults.getLimit(),
                customerSearchDefaults.getSortField(),
                customerSearchDefaults.getDirection());

        if (!StringUtils.hasText(name)) {
            return ResponseEntity.status(HttpStatus.OK).body(customerService.findCustomers(pageable));
        } else {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(customerService.findCustomersNameContainingSubString(pageable, name));
        }
    }

    @Override
    public ResponseEntity<CustomerDTO> getCustomerById(Long id) {
        log.debug("GET /customer/{} called", id);
        return ResponseEntity.ok(customerService.findCustomerById(id));
    }

    @Override
    public ResponseEntity<CustomerDTO> createCustomer(CustomerRequestDTO customerRequestDTO) {
        log.debug("POST /customers called");
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.createCustomer(customerRequestDTO));
    }
}

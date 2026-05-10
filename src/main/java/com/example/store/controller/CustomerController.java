package com.example.store.controller;

import com.example.store.component.CustomerSearchProps;
import com.example.store.dto.CustomerDTO;
import com.example.store.dto.CustomerRequestDTO;
import com.example.store.dto.SortEnumDTO;
import com.example.store.service.CustomerService;
import com.example.store.util.PageableBuilder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerSearchProps customerSearchProps;
    private final PageableBuilder pageableBuilder;

    @GetMapping
    public ResponseEntity<List<CustomerDTO>> findCustomers(
            @RequestParam(required = false) final String name,
            @RequestParam(required = false) @Min(value = 0, message = "Min page number is 0") final Integer page,
            @RequestParam(required = false) @Min(value = 5, message = "Min limit is 5") final Integer limit,
            @RequestParam(required = false) final String sortBy,
            @RequestParam(required = false) final SortEnumDTO sortDir) {

        final Pageable pageable = pageableBuilder.buildPageable(page, limit, sortBy, sortDir,
                customerSearchProps.getLimit(),
                customerSearchProps.getSortField(),
                customerSearchProps.getDirection());

        if (!StringUtils.hasText(name)) {
            return ResponseEntity.status(HttpStatus.OK).body(customerService.findCustomers(pageable));
        } else {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(customerService.findCustomersNameContainingSubString(pageable, name));
        }
    }

    @GetMapping("{id}")
    public ResponseEntity<CustomerDTO> findCustomerById(@PathVariable @Min(value=1, message = "Invalid ID value. Please enter a valid ID") final Long id){
        return ResponseEntity.ok(customerService.findCustomerById(id));
    }

    @PostMapping
    public ResponseEntity<CustomerDTO> createCustomer(@RequestBody @Valid final CustomerRequestDTO customerRequestDTO) {

        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.createCustomer(customerRequestDTO));
    }
}

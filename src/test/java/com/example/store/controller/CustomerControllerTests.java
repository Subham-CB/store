package com.example.store.controller;

import com.example.store.api.model.CustomerDTO;
import com.example.store.api.model.CustomerRequestDTO;
import com.example.store.component.CustomerSearchDefaults;
import com.example.store.mapper.CustomerMapper;
import com.example.store.service.CustomerService;
import com.example.store.util.PageableBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
@ComponentScan(basePackageClasses = CustomerMapper.class)
class CustomerControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    private CustomerSearchDefaults customerSearchDefaults;

    @MockitoBean
    private PageableBuilder pageableBuilder;

    private CustomerDTO customerDTO;
    private Pageable mockPageable;

    @BeforeEach
    void setUp() {
        customerDTO = new CustomerDTO();
        customerDTO.setName("John Doe");
        customerDTO.setId(1L);

        mockPageable = PageRequest.of(0, 10);

        when(customerSearchDefaults.getLimit()).thenReturn(20);
        when(customerSearchDefaults.getSortField()).thenReturn("name");
        when(customerSearchDefaults.getDirection()).thenReturn("asc");

        when(pageableBuilder.buildPageable(any(), any(), any(), any(), anyInt(), anyString(), anyString()))
                .thenReturn(mockPageable);
    }

    @Test
    @DisplayName("POST /customer - Should return 201 Created")
    void testCreateCustomer() throws Exception {

        CustomerRequestDTO customerRequestDTO = new CustomerRequestDTO();
        customerRequestDTO.setName("John Doe");

        when(customerService.createCustomer(customerRequestDTO)).thenReturn(customerDTO);

        mockMvc.perform(post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @DisplayName("GET /customer - Find all (no name param)")
    void testGetCustomers_All() throws Exception {
        when(customerService.findCustomers(mockPageable)).thenReturn(List.of(customerDTO));

        mockMvc.perform(get("/customer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("John Doe"));
    }

    @Test
    @DisplayName("GET /customer - Search by name")
    void testGetCustomers_WithName() throws Exception {
        String searchName = "John";
        when(customerService.findCustomersNameContainingSubString(mockPageable, searchName))
                .thenReturn(List.of(customerDTO));

        mockMvc.perform(get("/customer").param("name", searchName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("John Doe"));
    }
}

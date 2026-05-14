package com.example.store.controller;

import com.example.store.api.model.CustomerDTO;
import com.example.store.api.model.CustomerRequestDTO;
import com.example.store.component.CustomerSearchDefaults;
import com.example.store.exception.CustomerNotFoundException;
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

        mockPageable = PageRequest.of(0, 20);

        when(customerSearchDefaults.getLimit()).thenReturn(20);
        when(customerSearchDefaults.getSortField()).thenReturn("name");
        when(customerSearchDefaults.getDirection()).thenReturn("asc");

        when(pageableBuilder.buildPageable(any(), any(), any(), any(), anyInt(), anyString(), anyString()))
                .thenReturn(mockPageable);
    }

    @Test
    @DisplayName("POST /customer - Should return 201 Created")
    void createCustomer_validRequest_returns201() throws Exception {

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
    @DisplayName("POST /customer - returns 400 when name is missing")
    void createCustomer_missingName_returns400() throws Exception {
        CustomerRequestDTO request = new CustomerRequestDTO();

        mockMvc.perform(post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.name").exists());
    }

    @Test
    @DisplayName("POST /customer - returns 400 when request body is missing entirely")
    void createCustomer_noBody_returns400() throws Exception {
        mockMvc.perform(post("/customer").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("GET /customer - returns 200 with list when no name param")
    void getCustomers_noNameParam_returnsFullList() throws Exception {
        when(customerService.findCustomers(mockPageable)).thenReturn(List.of(customerDTO));

        mockMvc.perform(get("/customer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("John Doe"));
    }

    @Test
    @DisplayName("GET /customer - returns 200 with empty array when no customers exist")
    void getCustomers_noCustomers_returnsEmptyList() throws Exception {
        when(customerService.findCustomers(mockPageable)).thenReturn(List.of());

        mockMvc.perform(get("/customer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /customer?name=John - delegates to search service and returns 200")
    void getCustomers_withNameParam() throws Exception {
        String searchName = "John";
        when(customerService.findCustomersNameContainingSubString(mockPageable, searchName))
                .thenReturn(List.of(customerDTO));

        mockMvc.perform(get("/customer").param("name", searchName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("John Doe"));
    }

    @Test
    @DisplayName("GET /customer?name=NoMatch - returns 200 with empty array when no match")
    void getCustomers_withNameParam_noMatch_returnsEmptyList() throws Exception {
        when(customerService.findCustomersNameContainingSubString(mockPageable, "NoMatch"))
                .thenReturn(List.of());

        mockMvc.perform(get("/customer").param("name", "NoMatch"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /customer?sortDir=INVALID - returns 400 for invalid sort direction enum")
    void getCustomers_invalidSortDir_returns400() throws Exception {
        mockMvc.perform(get("/customer").param("sortDir", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("GET /customer/{id} - returns 200 with correct customer body")
    void getCustomerById_existingId_returns200() throws Exception {
        when(customerService.findCustomerById(1L)).thenReturn(customerDTO);

        mockMvc.perform(get("/customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @DisplayName("GET /customer/{id} - returns 404 when customer does not exist")
    void getCustomerById_nonExistentId_returns404() throws Exception {
        when(customerService.findCustomerById(999L)).thenThrow(new CustomerNotFoundException(999L));

        mockMvc.perform(get("/customer/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Customer not found with Id : 999"));
    }
}

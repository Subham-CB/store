package com.example.store.service;

import com.example.store.api.model.CustomerDTO;
import com.example.store.api.model.CustomerRequestDTO;
import com.example.store.entity.Customer;
import com.example.store.exception.CustomerNotFoundException;
import com.example.store.mapper.CustomerMapper;
import com.example.store.repository.CustomerRepository;
import com.example.store.service.impl.CustomerServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer customer;
    private CustomerDTO customerDTO;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");

        customerDTO = new CustomerDTO();
        customerDTO.setId(1L);
        customerDTO.setName("John Doe");

        pageable = PageRequest.of(0, 20);
    }

    @Test
    @DisplayName("findCustomers - returns mapped list from repository page")
    void findCustomers_returnsMappedList() {

        Page<Customer> page = new PageImpl<>(List.of(customer));
        when(customerRepository.findAll(pageable)).thenReturn(page);
        when(customerMapper.customersToCustomerDTOs(List.of(customer))).thenReturn(List.of(customerDTO));

        List<CustomerDTO> result = customerService.findCustomers(pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(1L);
        assertThat(result.getFirst().getName()).isEqualTo("John Doe");

        verify(customerRepository).findAll(pageable);
        verify(customerMapper).customersToCustomerDTOs(List.of(customer));
    }

    @Test
    @DisplayName("findCustomers - returns empty list when no customers exist")
    void findCustomers_emptyPage_returnsEmptyList() {
        Page<Customer> emptyPage = new PageImpl<>(List.of());
        when(customerRepository.findAll(pageable)).thenReturn(emptyPage);
        when(customerMapper.customersToCustomerDTOs(List.of())).thenReturn(List.of());

        List<CustomerDTO> result = customerService.findCustomers(pageable);

        assertThat(result).isEmpty();
        verify(customerRepository).findAll(pageable);
    }

    @Test
    @DisplayName("findCustomerById - returns mapped DTO when customer exists")
    void findCustomerById_existingId_returnsMappedDTO() {
        when(customerRepository.findCustomerById(1L)).thenReturn(Optional.of(customer));
        when(customerMapper.customerToCustomerDTO(customer)).thenReturn(customerDTO);

        CustomerDTO result = customerService.findCustomerById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John Doe");

        verify(customerRepository).findCustomerById(1L);
        verify(customerMapper).customerToCustomerDTO(customer);
    }

    @Test
    @DisplayName("findCustomerById - throws CustomerNotFoundException when id does not exist")
    void findCustomerById_nonExistentId_throwCustomerNotFoundException() {

        assertThatThrownBy(() -> customerService.findCustomerById(2L))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("2");

        verify(customerRepository).findCustomerById(2L);
        verifyNoInteractions(customerMapper);
    }

    @Test
    @DisplayName("findCustomersNameContainingSubString - returns matched customers")
    void findCustomersNameContainingSubString_match_returnsList() {
        when(customerRepository.findCustomersByNameContainingIgnoreCase(pageable, "John"))
                .thenReturn(List.of(customer));
        when(customerMapper.customersToCustomerDTOs(List.of(customer))).thenReturn(List.of(customerDTO));

        List<CustomerDTO> result = customerService.findCustomersNameContainingSubString(pageable, "John");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("John Doe");

        verify(customerRepository).findCustomersByNameContainingIgnoreCase(pageable, "John");
    }

    @Test
    @DisplayName("findCustomersNameContainingSubString - returns empty list when no match")
    void findCustomersNameContainingSubString_noMatch_returnsEmptyList() {
        when(customerRepository.findCustomersByNameContainingIgnoreCase(pageable, "Test"))
                .thenReturn(List.of());
        when(customerMapper.customersToCustomerDTOs(List.of())).thenReturn(List.of());

        List<CustomerDTO> result = customerService.findCustomersNameContainingSubString(pageable, "Test");

        assertThat(result).isEmpty();

        verify(customerRepository).findCustomersByNameContainingIgnoreCase(pageable, "Test");
    }

    @Test
    @DisplayName("createCustomer - maps request, saves entity, returns mapped DTO")
    void createCustomer_savesAndReturnsMappedDTO() {
        CustomerRequestDTO request = new CustomerRequestDTO();
        request.setName("New Customer");

        Customer newCustomer = new Customer();
        newCustomer.setName("New Customer");

        Customer savedCustomer = new Customer();
        savedCustomer.setId(10L);
        savedCustomer.setName("New Customer");

        CustomerDTO savedDTO = new CustomerDTO();
        savedDTO.setId(10L);
        savedDTO.setName("New Customer");

        when(customerMapper.customerRequestDTOToCustomer(request)).thenReturn(newCustomer);
        when(customerRepository.save(newCustomer)).thenReturn(savedCustomer);
        when(customerMapper.customerToCustomerDTO(savedCustomer)).thenReturn(savedDTO);

        CustomerDTO result = customerService.createCustomer(request);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("New Customer");

        verify(customerMapper).customerRequestDTOToCustomer(request);
        verify(customerRepository).save(newCustomer);
        verify(customerMapper).customerToCustomerDTO(savedCustomer);
    }
}

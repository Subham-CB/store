package com.example.store.mapper;

import com.example.store.api.model.CustomerDTO;
import com.example.store.api.model.CustomerOrderDTO;
import com.example.store.api.model.CustomerRequestDTO;
import com.example.store.api.model.OrderCustomerDTO;
import com.example.store.entity.Customer;
import com.example.store.entity.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@Import(CustomerMapperImpl.class)
public class CustomerMapperTest {

    @Autowired
    private CustomerMapper customerMapper;


    @Test
    @DisplayName("customerToCustomerDTO - maps id and name from Customer")
    void customerToCustomerDTO_mapsIdAndName() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("Muriel Donnelly");

        CustomerDTO result = customerMapper.customerToCustomerDTO(customer);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Muriel Donnelly");
    }

    @Test
    @DisplayName("customerToCustomerDTO - maps nested orders to list of CustomerOrderDTO")
    void customerToCustomerDTO_mapsOrdersToCustomerOrderDTOs() {
        Order order = new Order();
        order.setId(10L);
        order.setDescription("Test Order");

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("Muriel Donnelly");
        customer.getOrders().add(order);

        CustomerDTO result = customerMapper.customerToCustomerDTO(customer);

        assertThat(result.getOrders()).hasSize(1);
        CustomerOrderDTO orderDTO = result.getOrders().get(0);
        assertThat(orderDTO.getId()).isEqualTo(10L);
        assertThat(orderDTO.getDescription()).isEqualTo("Test Order");
    }

    @Test
    @DisplayName("customerToCustomerDTO - maps empty orders to empty list")
    void customerToCustomerDTO_emptyOrders_mapsToEmptyList() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("Muriel Donnelly");

        CustomerDTO result = customerMapper.customerToCustomerDTO(customer);

        assertThat(result.getOrders()).isNotNull();
        assertThat(result.getOrders()).isEmpty();
    }

    @Test
    @DisplayName("customerToCustomerDTO - maps multiple orders correctly")
    void customerToCustomerDTO_multipleOrders_allMapped() {
        Order order1 = new Order();
        order1.setId(1L);
        order1.setDescription("First Order");

        Order order2 = new Order();
        order2.setId(2L);
        order2.setDescription("Second Order");

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("Test Customer");
        customer.getOrders().addAll(Set.of(order1, order2));

        CustomerDTO result = customerMapper.customerToCustomerDTO(customer);

        assertThat(result.getOrders()).hasSize(2);
        assertThat(result.getOrders())
                .extracting(CustomerOrderDTO::getId)
                .containsExactlyInAnyOrder(1L, 2L);
    }


    @Test
    @DisplayName("customerToOrderCustomerDTO - maps id and name only")
    void customerToOrderCustomerDTO_mapsIdAndName() {
        Customer customer = new Customer();
        customer.setId(5L);
        customer.setName("Vicki Kutch");

        OrderCustomerDTO result = customerMapper.customerToOrderCustomerDTO(customer);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getName()).isEqualTo("Vicki Kutch");
    }


    @Test
    @DisplayName("customersToCustomerDTOs - maps list of customers to list of DTOs")
    void customersToCustomerDTOs_mapsList() {
        Customer c1 = new Customer();
        c1.setId(1L);
        c1.setName("Alice");

        Customer c2 = new Customer();
        c2.setId(2L);
        c2.setName("Bob");

        List<CustomerDTO> result = customerMapper.customersToCustomerDTOs(List.of(c1, c2));

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(CustomerDTO::getName)
                .containsExactly("Alice", "Bob");
    }

    @Test
    @DisplayName("customersToCustomerDTOs - returns empty list when input is empty")
    void customersToCustomerDTOs_emptyList_returnsEmptyList() {
        List<CustomerDTO> result = customerMapper.customersToCustomerDTOs(List.of());

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }


    @Test
    @DisplayName("customerRequestDTOToCustomer - maps name from request")
    void customerRequestDTOToCustomer_mapsName() {
        CustomerRequestDTO request = new CustomerRequestDTO();
        request.setName("New Customer");

        Customer result = customerMapper.customerRequestDTOToCustomer(request);

        assertThat(result.getName()).isEqualTo("New Customer");
    }

    @Test
    @DisplayName("customerRequestDTOToCustomer - id is null (ignored)")
    void customerRequestDTOToCustomer_idIsNull() {
        CustomerRequestDTO request = new CustomerRequestDTO();
        request.setName("New Customer");

        Customer result = customerMapper.customerRequestDTOToCustomer(request);

        // @Mapping(target = "id", ignore = true)
        assertThat(result.getId()).isNull();
    }

    @Test
    @DisplayName("customerRequestDTOToCustomer - orders is empty (ignored)")
    void customerRequestDTOToCustomer_ordersIsEmpty() {
        CustomerRequestDTO request = new CustomerRequestDTO();
        request.setName("New Customer");

        Customer result = customerMapper.customerRequestDTOToCustomer(request);

        // @Mapping(target = "orders", ignore = true)
        assertThat(result.getOrders()).isNotNull();
        assertThat(result.getOrders()).isEmpty();
    }


    @Test
    @DisplayName("orderToCustomerOrderDTO - maps id and description from Order")
    void orderToCustomerOrderDTO_mapsIdAndDescription() {
        Order order = new Order();
        order.setId(7L);
        order.setDescription("Handcrafted Soft Chair");

        CustomerOrderDTO result = customerMapper.orderToCustomerOrderDTO(order);

        assertThat(result.getId()).isEqualTo(7L);
        assertThat(result.getDescription()).isEqualTo("Handcrafted Soft Chair");
    }
}

package com.example.store.repository;

import com.example.store.entity.Customer;
import com.example.store.entity.Order;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class CustomerRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Customer customerWithOrders;
    private Customer customerWithoutOrders;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        customerWithOrders = new Customer();
        customerWithOrders.setName("John Test Customer");

        entityManager.persist(customerWithOrders);

        Order order = new Order();
        order.setDescription("Test Order");
        order.setCustomer(customerWithOrders);

        entityManager.persist(order);

        customerWithoutOrders = new Customer();
        customerWithoutOrders.setName("New Test Customer");

        entityManager.persist(customerWithoutOrders);

        entityManager.flush();
        entityManager.clear();

        pageable = PageRequest.of(0, 20);
    }

    @Test
    @DisplayName("findCustomersByNameContainingIgnoreCase - returns customers whose name contains substring")
    void findCustomersByNameContainingIgnoreCase_match_returnsCustomers() {

        List<Customer> result = customerRepository.findCustomersByNameContainingIgnoreCase(pageable, "John");

        assertThat(result).isNotEmpty();
        assertThat(result).extracting(Customer::getName).allMatch(name -> name.toLowerCase()
                .contains("john"));
    }

    @Test
    @DisplayName("findCustomersByNameContainingIgnoreCase - is case insensitive (uppercase search)")
    void findCustomersByNameContainingIgnoreCase_upperCaseSearch_stillMatches() {
        List<Customer> result = customerRepository.findCustomersByNameContainingIgnoreCase(pageable, "JOHN");

        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("findCustomersByNameContainingIgnoreCase - returns empty list when no match")
    void findCustomersByNameContainingIgnoreCase_noMatch_returnsEmptyList() {
        List<Customer> result = customerRepository.findCustomersByNameContainingIgnoreCase(pageable, "NOMATCH");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findCustomersByNameContainingIgnoreCase - respects pageable limit")
    void findCustomersByNameContainingIgnoreCase_respectsPageableLimit() {

        for (int i = 0; i < 3; i++) {
            Customer c = new Customer();
            c.setName("John Extra " + i);
            entityManager.persist(c);
        }
        entityManager.flush();
        entityManager.clear();

        List<Customer> result =
                customerRepository.findCustomersByNameContainingIgnoreCase(PageRequest.of(0, 2), "Glover");

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("findCustomerById - returns customer with orders eagerly loaded via LEFT JOIN FETCH")
    void findCustomerById_existingId_returnsCustomerWithOrdersLoaded() {
        Optional<Customer> result = customerRepository.findCustomerById(customerWithOrders.getId());

        log.info(customerWithOrders.getId().toString());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("John Test Customer");
        assertThat(result.get().getOrders()).hasSize(1);
        assertThat(result.get().getOrders()).extracting(Order::getDescription).containsExactly("Test Order");
    }

    @Test
    @DisplayName("findCustomerById - returns customer with empty orders set when customer has no orders")
    void findCustomerById_customerWithNoOrders_returnsEmptyOrdersSet() {
        Optional<Customer> result = customerRepository.findCustomerById(customerWithoutOrders.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getOrders()).isNotNull();
        assertThat(result.get().getOrders()).isEmpty();
    }

    @Test
    @DisplayName("findCustomerById - returns empty Optional when id does not exist")
    void findCustomerById_nonExistentId_returnsEmptyOptional() {
        Optional<Customer> result = customerRepository.findCustomerById(999999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findCustomerById - with multiple orders")
    void findCustomerById_customerWithMultipleOrders_returnsExactlyOneCustomer() {
        Order order2 = new Order();
        order2.setDescription("Second Order");
        order2.setCustomer(customerWithOrders);
        entityManager.persist(order2);
        entityManager.flush();
        entityManager.clear();

        Optional<Customer> result = customerRepository.findCustomerById(customerWithOrders.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getOrders()).hasSize(2);
    }
}

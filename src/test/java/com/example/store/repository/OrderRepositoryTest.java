package com.example.store.repository;

import com.example.store.entity.Customer;
import com.example.store.entity.Order;
import com.example.store.entity.Product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Order orderWithProducts;
    private Order orderWithoutProducts;
    private Customer customer;
    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setName("Test Customer");
        entityManager.persist(customer);

        product1 = new Product();
        product1.setDescription("Product One");
        entityManager.persist(product1);

        product2 = new Product();
        product2.setDescription("Product Two");
        entityManager.persist(product2);

        orderWithProducts = new Order();
        orderWithProducts.setDescription("Order With Products");
        orderWithProducts.setCustomer(customer);
        orderWithProducts.getProducts().add(product1);
        orderWithProducts.getProducts().add(product2);
        entityManager.persist(orderWithProducts);

        orderWithoutProducts = new Order();
        orderWithoutProducts.setDescription("Order Without Products");
        orderWithoutProducts.setCustomer(customer);
        entityManager.persist(orderWithoutProducts);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("findOrderById - returns order with customer")
    void findOrderById_existingId_returnsOrderWithCustomerLoaded() {
        Optional<Order> result = orderRepository.findOrderById(orderWithProducts.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getDescription()).isEqualTo("Order With Products");

        assertThat(result.get().getCustomer()).isNotNull();
        assertThat(result.get().getCustomer().getName()).isEqualTo("Test Customer");
    }

    @Test
    @DisplayName("findOrderById - returns order with products eagerly loaded via LEFT JOIN FETCH")
    void findOrderById_existingId_returnsOrderWithProductsLoaded() {
        Optional<Order> result = orderRepository.findOrderById(orderWithProducts.getId());

        assertThat(result).isPresent();

        assertThat(result.get().getProducts()).hasSize(2);
        assertThat(result.get().getProducts())
                .extracting(Product::getDescription)
                .containsExactlyInAnyOrder("Product One", "Product Two");
    }

    @Test
    @DisplayName("findOrderById - returns empty Optional when id does not exist")
    void findOrderById_nonExistentId_returnsEmptyOptional() {
        Optional<Order> result = orderRepository.findOrderById(999999L);

        assertThat(result).isEmpty();
    }
}

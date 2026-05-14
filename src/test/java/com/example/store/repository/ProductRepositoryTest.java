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

public class ProductRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Product productWithOrders;
    private Product productWithoutOrders;

    @BeforeEach
    void setUp() {
        Customer customer = new Customer();
        customer.setName("Test Customer");
        entityManager.persist(customer);

        productWithOrders = new Product();
        productWithOrders.setDescription("Product With Orders");
        entityManager.persist(productWithOrders);

        productWithoutOrders = new Product();
        productWithoutOrders.setDescription("Product Without Orders");
        entityManager.persist(productWithoutOrders);

        Order order1 = new Order();
        order1.setDescription("First Order");
        order1.setCustomer(customer);
        order1.getProducts().add(productWithOrders);
        entityManager.persist(order1);

        Order order2 = new Order();
        order2.setDescription("Second Order");
        order2.setCustomer(customer);
        order2.getProducts().add(productWithOrders);
        entityManager.persist(order2);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("findProductById - returns product with orders eagerly loaded via LEFT JOIN FETCH")
    void findProductById_existingId_returnsProductWithOrdersLoaded() {
        Optional<Product> result = productRepository.findProductById(productWithOrders.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getDescription()).isEqualTo("Product With Orders");

        assertThat(result.get().getOrders()).hasSize(2);
        assertThat(result.get().getOrders())
                .extracting(Order::getDescription)
                .containsExactlyInAnyOrder("First Order", "Second Order");
    }

    @Test
    @DisplayName("findProductById - returns product with empty orders set when product has no orders")
    void findProductById_productWithNoOrders_returnsEmptyOrdersSet() {
        Optional<Product> result = productRepository.findProductById(productWithoutOrders.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getDescription()).isEqualTo("Product Without Orders");

        assertThat(result.get().getOrders()).isNotNull();
        assertThat(result.get().getOrders()).isEmpty();
    }

    @Test
    @DisplayName("findProductById - returns empty Optional when id does not exist")
    void findProductById_nonExistentId_returnsEmptyOptional() {
        Optional<Product> result = productRepository.findProductById(999999L);

        assertThat(result).isEmpty();
    }
}

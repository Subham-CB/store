package com.example.store.mapper;

import com.example.store.api.model.OrderDTO;
import com.example.store.api.model.OrderProductDTO;
import com.example.store.api.model.OrderRequestDTO;
import com.example.store.entity.Customer;
import com.example.store.entity.Order;
import com.example.store.entity.Product;
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
@Import({OrderMapperImpl.class,
CustomerMapperImpl.class,
ProductMapperTest.class})
public class OrderMapperTest {

    @Autowired
    private OrderMapper orderMapper;


    @Test
    @DisplayName("orderToOrderDTO - maps id and description from Order")
    void orderToOrderDTO_mapsIdAndDescription() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("Muriel Donnelly");

        Order order = new Order();
        order.setId(1L);
        order.setDescription("Test Order");
        order.setCustomer(customer);

        OrderDTO result = orderMapper.orderToOrderDTO(order);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("Test Order");
    }

    @Test
    @DisplayName("orderToOrderDTO - maps customer via CustomerMapper to OrderCustomerDTO")
    void orderToOrderDTO_mapsCustomerToOrderCustomerDTO() {
        Customer customer = new Customer();
        customer.setId(6L);
        customer.setName("Vicki Kutch");

        Order order = new Order();
        order.setId(1L);
        order.setDescription("Test Order");
        order.setCustomer(customer);

        OrderDTO result = orderMapper.orderToOrderDTO(order);

        // uses = {CustomerMapper.class} — CustomerMapper.customerToOrderCustomerDTO is called
        assertThat(result.getCustomer()).isNotNull();
        assertThat(result.getCustomer().getId()).isEqualTo(6L);
        assertThat(result.getCustomer().getName()).isEqualTo("Vicki Kutch");
    }

    @Test
    @DisplayName("orderToOrderDTO - maps products to Set of product ids via map() default method")
    void orderToOrderDTO_mapsProductsToProductIds() {
        Product product1 = new Product();
        product1.setId(1L);
        product1.setDescription("Product One");

        Product product2 = new Product();
        product2.setId(5L);
        product2.setDescription("Product Two");

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("Test Customer");

        Order order = new Order();
        order.setId(1L);
        order.setDescription("Test Order");
        order.setCustomer(customer);
        order.getProducts().addAll(Set.of(product1, product2));

        OrderDTO result = orderMapper.orderToOrderDTO(order);

        // default Set<Long> map(Set<Product> products) extracts product ids
        assertThat(result.getProducts())
                .extracting(OrderProductDTO::getId)
                .containsExactlyInAnyOrder(1L, 5L);
    }



    @Test
    @DisplayName("ordersToOrderDTOs - maps list of orders to list of DTOs")
    void ordersToOrderDTOs_mapsList() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("Test Customer");

        Order order1 = new Order();
        order1.setId(1L);
        order1.setDescription("First Order");
        order1.setCustomer(customer);

        Order order2 = new Order();
        order2.setId(2L);
        order2.setDescription("Second Order");
        order2.setCustomer(customer);

        List<OrderDTO> result = orderMapper.ordersToOrderDTOs(List.of(order1, order2));

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(OrderDTO::getId)
                .containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("ordersToOrderDTOs - returns empty list when input is empty")
    void ordersToOrderDTOs_emptyList_returnsEmptyList() {
        List<OrderDTO> result = orderMapper.ordersToOrderDTOs(List.of());

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }


    @Test
    @DisplayName("orderRequestDTOToOrder - maps description from request")
    void orderRequestDTOToOrder_mapsDescription() {
        OrderRequestDTO request = new OrderRequestDTO();
        request.setDescription("New Order");
        request.setCustomerId(1L);
        request.setProductIds(Set.of(1L, 2L));

        Order result = orderMapper.orderRequestDTOToOrder(request);

        assertThat(result.getDescription()).isEqualTo("New Order");
    }

    @Test
    @DisplayName("orderRequestDTOToOrder - id is null ")
    void orderRequestDTOToOrder_idIsNull() {
        OrderRequestDTO request = new OrderRequestDTO();
        request.setDescription("New Order");
        request.setCustomerId(1L);
        request.setProductIds(Set.of(1L));

        Order result = orderMapper.orderRequestDTOToOrder(request);

        // @Mapping(target = "id", ignore = true)
        assertThat(result.getId()).isNull();
    }

    @Test
    @DisplayName("orderRequestDTOToOrder - customer is null ")
    void orderRequestDTOToOrder_customerIsNull() {
        OrderRequestDTO request = new OrderRequestDTO();
        request.setDescription("New Order");
        request.setCustomerId(1L);
        request.setProductIds(Set.of(1L));

        Order result = orderMapper.orderRequestDTOToOrder(request);

        // @Mapping(target = "customer", ignore = true)
        assertThat(result.getCustomer()).isNull();
    }

    @Test
    @DisplayName("orderRequestDTOToOrder - products is empty")
    void orderRequestDTOToOrder_productsIsEmpty() {
        OrderRequestDTO request = new OrderRequestDTO();
        request.setDescription("New Order");
        request.setCustomerId(1L);
        request.setProductIds(Set.of(1L));

        Order result = orderMapper.orderRequestDTOToOrder(request);

        // @Mapping(target = "products", ignore = true)
        assertThat(result.getProducts()).isNotNull();
        assertThat(result.getProducts()).isEmpty();
    }


    @Test
    @DisplayName("map(Set<Product>) - extracts id from each Product in the set")
    void map_productsToIds_extractsIdsCorrectly() {
        Product p1 = new Product();
        p1.setId(1L);

        Product p2 = new Product();
        p2.setId(5L);

        Set<Long> result = orderMapper.map(Set.of(p1, p2));

        assertThat(result).containsExactlyInAnyOrder(1L, 5L);
    }

    @Test
    @DisplayName("map(Set<Product>) - returns empty set when input is empty")
    void map_emptySet_returnsEmptySet() {
        Set<Long> result = orderMapper.map(Set.of());

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

}
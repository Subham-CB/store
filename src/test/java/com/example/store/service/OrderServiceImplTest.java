package com.example.store.service;

import com.example.store.api.model.OrderCustomerDTO;
import com.example.store.api.model.OrderDTO;
import com.example.store.api.model.OrderRequestDTO;
import com.example.store.entity.Customer;
import com.example.store.entity.Order;
import com.example.store.entity.Product;
import com.example.store.exception.CustomerNotFoundException;
import com.example.store.exception.OrderNotFoundException;
import com.example.store.exception.ProductNotFoundException;
import com.example.store.mapper.OrderMapper;
import com.example.store.repository.CustomerRepository;
import com.example.store.repository.OrderRepository;
import com.example.store.repository.ProductRepository;
import com.example.store.service.impl.OrderServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Customer customer;
    private Product product1;
    private Product product2;
    private Order order;
    private OrderDTO orderDTO;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("Anil Kumar");

        product1 = new Product();
        product1.setId(1L);
        product1.setDescription("Mechanical Keyboard");

        product2 = new Product();
        product2.setId(2L);
        product2.setDescription("Wooden Chair");

        order = new Order();
        order.setId(1L);
        order.setDescription("Test Order");
        order.setCustomer(customer);
        order.setProducts(Set.of(product1, product2));

        OrderCustomerDTO orderCustomerDTO = new OrderCustomerDTO();
        orderCustomerDTO.setId(1L);
        orderCustomerDTO.setName("Anil Kumar");

        orderDTO = new OrderDTO();
        orderDTO.setId(1L);
        orderDTO.setDescription("Test Order");
        orderDTO.setCustomer(orderCustomerDTO);

        pageable = PageRequest.of(0, 30);
    }

    @Test
    @DisplayName("findAllOrders - returns mapped list from repository page")
    void findAllOrders_returnsMappedList() {
        when(orderRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(order)));
        when(orderMapper.ordersToOrderDTOs(List.of(order))).thenReturn(List.of(orderDTO));

        List<OrderDTO> result = orderService.findAllOrders(pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(1L);
        assertThat(result.getFirst().getDescription()).isEqualTo("Test Order");

        verify(orderRepository).findAll(pageable);
        verify(orderMapper).ordersToOrderDTOs(List.of(order));
    }

    @Test
    @DisplayName("findAllOrders - returns empty list when no orders exist")
    void findAllOrders_emptyPage_returnsEmptyList() {
        when(orderRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of()));
        when(orderMapper.ordersToOrderDTOs(List.of())).thenReturn(List.of());

        List<OrderDTO> result = orderService.findAllOrders(pageable);

        assertThat(result).isEmpty();
        verify(orderRepository).findAll(pageable);
    }

    @Test
    @DisplayName("findOrderById - returns mapped DTO when order exists")
    void findOrderById_existingId_returnsMappedDTO() {
        when(orderRepository.findOrderById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.orderToOrderDTO(order)).thenReturn(orderDTO);

        OrderDTO result = orderService.findOrderById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("Test Order");
        assertThat(result.getCustomer().getId()).isEqualTo(1L);

        verify(orderRepository).findOrderById(1L);
        verify(orderMapper).orderToOrderDTO(order);
    }

    @Test
    @DisplayName("findOrderById - throws OrderNotFoundException when id does not exist")
    void findOrderById_nonExistentId_throwsOrderNotFoundException() {
        when(orderRepository.findOrderById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findOrderById(999L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("999");

        verify(orderRepository).findOrderById(999L);
        verifyNoInteractions(orderMapper);
    }

    @Test
    @DisplayName("createOrder - saves order with customer and products, returns mapped DTO")
    void createOrder_validRequest_savesAndReturnsMappedDTO() {
        OrderRequestDTO request = new OrderRequestDTO();
        request.setDescription("Test Order");
        request.setCustomerId(1L);
        request.setProductIds(Set.of(1L, 2L));

        Order mappedOrder = new Order();
        mappedOrder.setDescription("Test Order");

        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setDescription("Test Order");
        savedOrder.setCustomer(customer);
        savedOrder.setProducts(Set.of(product1, product2));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(orderMapper.orderRequestDTOToOrder(request)).thenReturn(mappedOrder);
        when(productRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(product1, product2));
        when(orderRepository.save(mappedOrder)).thenReturn(savedOrder);
        when(orderMapper.orderToOrderDTO(savedOrder)).thenReturn(orderDTO);

        OrderDTO result = orderService.createOrder(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("Test Order");

        verify(customerRepository).findById(1L);
        verify(productRepository).findAllById(Set.of(1L, 2L));
        verify(orderRepository).save(mappedOrder);
    }

    @Test
    @DisplayName("createOrder - throws CustomerNotFoundException when customer does not exist")
    void createOrder_nonExistentCustomer_throwsCustomerNotFoundException() {
        OrderRequestDTO request = new OrderRequestDTO();
        request.setDescription("Test Order");
        request.setCustomerId(999L);
        request.setProductIds(Set.of(1L));

        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("999");

        verify(customerRepository).findById(999L);
        verifyNoInteractions(orderRepository);
        verifyNoInteractions(productRepository);
    }

    @Test
    @DisplayName("createOrder - throws ProductNotFoundException when all product IDs are missing")
    void createOrder_allProductsMissing_throwsProductNotFoundException() {
        OrderRequestDTO request = new OrderRequestDTO();
        request.setDescription("Test Order");
        request.setCustomerId(1L);
        request.setProductIds(Set.of(999L));

        Order mappedOrder = new Order();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(orderMapper.orderRequestDTOToOrder(request)).thenReturn(mappedOrder);
        // repository returns empty — none of the requested products exist
        when(productRepository.findAllById(Set.of(999L))).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("999");

        verifyNoInteractions(orderRepository);
    }

    @Test
    @DisplayName("createOrder - throws ProductNotFoundException when some product IDs are missing")
    void createOrder_someProductsMissing_throwsProductNotFoundException() {
        OrderRequestDTO request = new OrderRequestDTO();
        request.setDescription("Test Order");
        request.setCustomerId(1L);
        request.setProductIds(Set.of(1L, 999L)); // 999 does not exist

        Order mappedOrder = new Order();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(orderMapper.orderRequestDTOToOrder(request)).thenReturn(mappedOrder);
        // Only product 1 is found — 999 is missing
        when(productRepository.findAllById(Set.of(1L, 999L))).thenReturn(List.of(product1));

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("999");

        verifyNoInteractions(orderRepository);
    }
}

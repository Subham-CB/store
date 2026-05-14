package com.example.store.mapper;

import com.example.store.api.model.OrderProductDTO;
import com.example.store.api.model.ProductDTO;
import com.example.store.api.model.ProductRequestDTO;
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
@Import(ProductMapperImpl.class)
public class ProductMapperTest {

    @Autowired
    private ProductMapper productMapper;

    @Test
    @DisplayName("productToProductDTO - maps id and description from Product")
    void productToProductDTO_mapsIdAndDescription() {
        Product product = new Product();
        product.setId(1L);
        product.setDescription("Ergonomic Steel Keyboard");

        ProductDTO result = productMapper.productToProductDTO(product);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("Ergonomic Steel Keyboard");
    }

    @Test
    @DisplayName("productToProductDTO - maps orders to orderIds Set via mapOrdersToIds")
    void productToProductDTO_mapsOrdersToOrderIds() {
        Order order1 = new Order();
        order1.setId(1L);

        Order order2 = new Order();
        order2.setId(8L);

        Product product = new Product();
        product.setId(1L);
        product.setDescription("Ergonomic Steel Keyboard");
        product.getOrders().addAll(Set.of(order1, order2));

        ProductDTO result = productMapper.productToProductDTO(product);

        // @Mapping(target = "orderIds", source = "orders", qualifiedByName = "mapOrdersToIds")
        assertThat(result.getOrderIds()).containsExactlyInAnyOrder(1L, 8L);
    }

    @Test
    @DisplayName("productToProductDTO - orderIds is empty set when product has no orders")
    void productToProductDTO_noOrders_returnsEmptyOrderIds() {
        Product product = new Product();
        product.setId(2L);
        product.setDescription("No Orders Product");

        ProductDTO result = productMapper.productToProductDTO(product);

        assertThat(result.getOrderIds()).isNotNull();
        assertThat(result.getOrderIds()).isEmpty();
    }

    @Test
    @DisplayName("productToOrderProductsDTO - maps id and description from Product")
    void productToOrderProductsDTO_mapsIdAndDescription() {
        Product product = new Product();
        product.setId(3L);
        product.setDescription("Rustic Wooden Chair");

        OrderProductDTO result = productMapper.productToOrderProductsDTO(product);

        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getDescription()).isEqualTo("Rustic Wooden Chair");
    }

    @Test
    @DisplayName("productsToProductDTOs - maps list of products to list of DTOs")
    void productsToProductDTOs_mapsList() {
        Product p1 = new Product();
        p1.setId(1L);
        p1.setDescription("Product One");

        Product p2 = new Product();
        p2.setId(2L);
        p2.setDescription("Product Two");

        List<ProductDTO> result = productMapper.productsToProductDTOs(List.of(p1, p2));

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ProductDTO::getId).containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("productsToProductDTOs - returns empty list when input is empty")
    void productsToProductDTOs_emptyList_returnsEmptyList() {
        List<ProductDTO> result = productMapper.productsToProductDTOs(List.of());

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("productRequestDTOToProduct - maps description from request")
    void productRequestDTOToProduct_mapsDescription() {
        ProductRequestDTO request = new ProductRequestDTO();
        request.setDescription("New Product");

        Product result = productMapper.productRequestDTOToProduct(request);

        assertThat(result.getDescription()).isEqualTo("New Product");
    }

    @Test
    @DisplayName("productRequestDTOToProduct - id is null (ignored)")
    void productRequestDTOToProduct_idIsNull() {
        ProductRequestDTO request = new ProductRequestDTO();
        request.setDescription("New Product");

        Product result = productMapper.productRequestDTOToProduct(request);

        // @Mapping(target = "id", ignore = true)
        assertThat(result.getId()).isNull();
    }

    @Test
    @DisplayName("productRequestDTOToProduct - orders is empty (ignored)")
    void productRequestDTOToProduct_ordersIsEmpty() {
        ProductRequestDTO request = new ProductRequestDTO();
        request.setDescription("New Product");

        Product result = productMapper.productRequestDTOToProduct(request);

        // @Mapping(target = "orders", ignore = true)
        assertThat(result.getOrders()).isNotNull();
        assertThat(result.getOrders()).isEmpty();
    }

    @Test
    @DisplayName("mapOrdersToIds - extracts id from each Order in the set")
    void mapOrdersToIds_extractsIdsCorrectly() {
        Order order1 = new Order();
        order1.setId(1L);

        Order order2 = new Order();
        order2.setId(5L);

        Order order3 = new Order();
        order3.setId(9L);

        Set<Long> result = productMapper.mapOrdersToIds(Set.of(order1, order2, order3));

        assertThat(result).containsExactlyInAnyOrder(1L, 5L, 9L);
    }

    @Test
    @DisplayName("mapOrdersToIds - returns empty set when input is empty")
    void mapOrdersToIds_emptySet_returnsEmptySet() {
        Set<Long> result = productMapper.mapOrdersToIds(Set.of());

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }
}

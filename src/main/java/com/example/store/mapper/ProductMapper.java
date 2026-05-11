package com.example.store.mapper;

import com.example.store.dto.order.OrderProductsDTO;
import com.example.store.dto.product.ProductDTO;
import com.example.store.dto.product.ProductRequestDTO;
import com.example.store.entity.Order;
import com.example.store.entity.Product;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "orderIds", source = "orders", qualifiedByName = "mapOrdersToIds")
    ProductDTO productToProductDTO(Product product);

    OrderProductsDTO productToOrderProductsDTO(Product product);

    List<ProductDTO> productsToProductDTOs(List<Product> products);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orders", ignore = true)
    Product productRequestDTOToProduct(ProductRequestDTO productRequestDTO);

    @Named("mapOrdersToIds")
    default Set<Long> mapOrdersToIds(Set<Order> orders) {
        return orders.stream().map(Order::getId).collect(Collectors.toSet());
    }
}

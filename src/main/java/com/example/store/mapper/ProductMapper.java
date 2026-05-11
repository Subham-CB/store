package com.example.store.mapper;

import com.example.store.dto.product.ProductDTO;
import com.example.store.dto.product.ProductRequestDTO;
import com.example.store.entity.Order;
import com.example.store.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "orderIds", source = "orders")
    ProductDTO productToProductDTO(Product product);

    List<ProductDTO> productsToProductDTOs(List<Product> products);

    @Mapping(target = "id",ignore = true)
    @Mapping(target = "orders",ignore = true)
    Product productRequestDTOToProduct(ProductRequestDTO productRequestDTO);


    default Set<Long> mapOrdersToIds(Set<Order> orders){
        return orders.stream()
                .map(Order::getId)
                .collect(Collectors.toSet());
    }
}

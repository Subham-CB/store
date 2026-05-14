package com.example.store.repository;

import com.example.store.entity.Product;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
    SELECT DISTINCT p
    FROM Product p
    LEFT JOIN FETCH p.orders
    WHERE p.id = :id
    """)
    Optional<Product> findProductById(Long id);

}

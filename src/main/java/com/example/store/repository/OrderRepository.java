package com.example.store.repository;

import com.example.store.entity.Order;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
    SELECT DISTINCT o
    FROM Order o
    LEFT JOIN FETCH o.customer
    LEFT JOIN FETCH o.products
    WHERE o.id = :id
    """)
    Optional<Order> findOrderById(Long id);

}

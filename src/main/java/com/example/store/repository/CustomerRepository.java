package com.example.store.repository;

import com.example.store.entity.Customer;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findCustomersByNameContainingIgnoreCase(Pageable pageable, String name);

    @Query("""
    SELECT DISTINCT c
    FROM Customer c
    LEFT JOIN FETCH c.orders
    WHERE c.id = :id
    """)
    Optional<Customer> findCustomerById(Long id);
}

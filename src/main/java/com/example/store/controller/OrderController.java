package com.example.store.controller;

import com.example.store.dto.OrderDTO;
import com.example.store.dto.OrderRequestDTO;
import com.example.store.service.OrderService;

import jakarta.validation.Valid;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.findAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable("id") @Min(value = 1,message = "Order ID must be at least 1") Long orderId){
        return ResponseEntity.status(HttpStatus.OK).body(orderService.findOrderById(orderId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<OrderDTO> createOrder(@RequestBody @Valid OrderRequestDTO orderRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(orderRequestDTO));
    }
}

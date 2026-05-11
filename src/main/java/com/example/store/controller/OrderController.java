package com.example.store.controller;

import com.example.store.component.GlobalSearchProp;
import com.example.store.dto.SortEnumDTO;
import com.example.store.dto.order.OrderDTO;
import com.example.store.dto.order.OrderRequestDTO;
import com.example.store.service.OrderService;
import com.example.store.util.PageableBuilder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
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
    private final PageableBuilder pageableBuilder;
    private final GlobalSearchProp globalSearchProp;

    @GetMapping
    public ResponseEntity<List<OrderDTO>> findAllOrders(
            @RequestParam(required = false) @Min(value = 0, message = "Min page number is 0") final Integer page,
            @RequestParam(required = false) @Min(value = 5, message = "Min limit is 5") final Integer limit,
            @RequestParam(required = false) final String sortBy,
            @RequestParam(required = false) final SortEnumDTO sortDir) {

        Pageable pageable = pageableBuilder.buildPageable(
                page,
                limit,
                sortBy,
                sortDir,
                globalSearchProp.getLimit(),
                globalSearchProp.getSortField(),
                globalSearchProp.getDirection());

        return ResponseEntity.status(HttpStatus.OK).body(orderService.findAllOrders(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(
            @PathVariable("id") @Min(value = 1, message = "Order ID must be at least 1") final Long orderId) {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.findOrderById(orderId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<OrderDTO> createOrder(@RequestBody @Valid final OrderRequestDTO orderRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(orderRequestDTO));
    }
}

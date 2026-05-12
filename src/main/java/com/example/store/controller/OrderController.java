package com.example.store.controller;

import com.example.store.api.OrderApi;
import com.example.store.api.model.OrderDTO;
import com.example.store.api.model.OrderRequestDTO;
import com.example.store.api.model.SortEnumDTO;
import com.example.store.component.GlobalSearchDefaults;
import com.example.store.service.OrderService;
import com.example.store.util.PageableBuilder;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderController implements OrderApi {

    private final OrderService orderService;
    private final PageableBuilder pageableBuilder;
    private final GlobalSearchDefaults globalSearchDefaults;

    @Override
    public ResponseEntity<List<OrderDTO>> getOrders(Integer page, Integer limit, String sortBy, SortEnumDTO sortDir) {

        Pageable pageable = pageableBuilder.buildPageable(
                page,
                limit,
                sortBy,
                sortDir,
                globalSearchDefaults.getLimit(),
                globalSearchDefaults.getSortField(),
                globalSearchDefaults.getDirection());

        return ResponseEntity.status(HttpStatus.OK).body(orderService.findAllOrders(pageable));
    }

    @Override
    public ResponseEntity<OrderDTO> getOrderById(Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.findOrderById(id));
    }

    @Override
    public ResponseEntity<OrderDTO> createOrder(OrderRequestDTO orderRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(orderRequestDTO));
    }
}

package com.example.store.dto;

import lombok.Data;

@Data
public class OrderRequestDTO {
    private String description;
    private Long customerId;
}

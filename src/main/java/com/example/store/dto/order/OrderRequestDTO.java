package com.example.store.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.util.Set;

@Data
public class OrderRequestDTO {

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Customer ID is required") private Long customerId;

    @NotNull(message = "Minimum 1 Product ID is required ") private Set<Long> productIds;
}

package com.example.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderRequestDTO {

    @NotBlank(message = "Description is required")
    private String description;
    @NotNull(message = "Customer ID is required")
    private Long customerId;
}

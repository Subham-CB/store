package com.example.store.dto.customer;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

import org.hibernate.validator.constraints.Length;

@Data
public class CustomerRequestDTO {

    @NotBlank(message = "Customer name is required")
    @Length(max = 225, message = "Name is too long") private String name;
}

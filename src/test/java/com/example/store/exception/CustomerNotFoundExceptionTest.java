package com.example.store.exception;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomerNotFoundExceptionTest {

    @Test
    @DisplayName("CustomerNotFoundException - message contains the customer id")
    void constructor_formatsMessageWithId() {
        CustomerNotFoundException ex = new CustomerNotFoundException(42L);

        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getMessage()).isEqualTo("Customer not found with Id : 42");
    }

    @Test
    @DisplayName("CustomerNotFoundException - message is distinct for different ids")
    void constructor_differentIds_produceDifferentMessages() {
        CustomerNotFoundException ex1 = new CustomerNotFoundException(1L);
        CustomerNotFoundException ex2 = new CustomerNotFoundException(999L);

        assertThat(ex1.getMessage()).isNotEqualTo(ex2.getMessage());
        assertThat(ex1.getMessage()).contains("1");
        assertThat(ex2.getMessage()).contains("999");
    }

}

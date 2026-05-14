package com.example.store.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderNotFoundExceptionTest {

    @Test
    @DisplayName("OrderNotFoundException - message contains the order id")
    void constructor_formatsMessageWithId() {
        OrderNotFoundException ex = new OrderNotFoundException(7L);

        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getMessage()).isEqualTo("Order not found with Id: 7");
    }

    @Test
    @DisplayName("OrderNotFoundException - message is distinct for different ids")
    void constructor_differentIds_produceDifferentMessages() {
        OrderNotFoundException ex1 = new OrderNotFoundException(1L);
        OrderNotFoundException ex2 = new OrderNotFoundException(999L);

        assertThat(ex1.getMessage()).isNotEqualTo(ex2.getMessage());
        assertThat(ex1.getMessage()).contains("1");
        assertThat(ex2.getMessage()).contains("999");
    }

}

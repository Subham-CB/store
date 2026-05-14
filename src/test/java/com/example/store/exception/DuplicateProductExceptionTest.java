package com.example.store.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DuplicateProductExceptionTest {

    @Test
    @DisplayName("DuplicateProductException - message contains the product description")
    void constructor_formatsMessageWithDescription() {
        DuplicateProductException ex = new DuplicateProductException("Mechanical Keyboard");

        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getMessage()).isEqualTo("A product with description 'Mechanical Keyboard' already exists");
    }

    @Test
    @DisplayName("DuplicateProductException - message is distinct for different descriptions")
    void constructor_differentDescriptions_produceDifferentMessages() {
        DuplicateProductException ex1 = new DuplicateProductException("Keyboard");
        DuplicateProductException ex2 = new DuplicateProductException("Mouse");

        assertThat(ex1.getMessage()).isNotEqualTo(ex2.getMessage());
        assertThat(ex1.getMessage()).contains("Keyboard");
        assertThat(ex2.getMessage()).contains("Mouse");
    }

}

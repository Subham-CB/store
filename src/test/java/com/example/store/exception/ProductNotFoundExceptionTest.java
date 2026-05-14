package com.example.store.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductNotFoundExceptionTest {

    @Test
    @DisplayName("ProductNotFoundException(Long) - message contains the single product id")
    void singleIdConstructor_formatsMessageWithId() {
        ProductNotFoundException ex = new ProductNotFoundException(15L);

        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getMessage()).isEqualTo("Product not found with Id : 15");
    }

    @Test
    @DisplayName("ProductNotFoundException(Set) - message contains all missing product ids")
    void setConstructor_formatsMessageWithAllIds() {
        ProductNotFoundException ex = new ProductNotFoundException(Set.of(10L, 20L));

        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getMessage()).startsWith("Product not found with Id :");
        assertThat(ex.getMessage()).contains("10");
        assertThat(ex.getMessage()).contains("20");
    }

}

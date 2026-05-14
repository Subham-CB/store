package com.example.store.exception.handler;

import com.example.store.api.model.ErrorResponseDTO;
import com.example.store.exception.CustomerNotFoundException;
import com.example.store.exception.OrderNotFoundException;
import com.example.store.exception.ProductNotFoundException;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.util.TypeInformation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn("/test-path");
    }

    private void assertCommonFields(ErrorResponseDTO body, int expectedStatus, String expectedError) {
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(expectedStatus);
        assertThat(body.getError()).isEqualTo(expectedError);
        assertThat(body.getPath()).isEqualTo("/test-path");
        assertThat(body.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("handleValidation - returns 400 with validationErrors map populated from field errors")
    void handleValidation_returns400WithValidationErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError = new FieldError("customerRequest", "name", "must not be blank");
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponseDTO> response = handler.handleValidation(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorResponseDTO body = response.getBody();
        assertCommonFields(body, 400, "Bad Request");
        assertThat(body.getMessage()).isEqualTo("Validation Failed");
        assertThat(body.getValidationErrors()).containsEntry("name", "must not be blank");
    }

    @Test
    @DisplayName("handleValidation - returns 400 with multiple field errors mapped correctly")
    void handleValidation_multipleFieldErrors_allMappedToValidationErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError nameError = new FieldError("req", "name", "must not be blank");
        FieldError idError = new FieldError("req", "customerId", "must not be null");
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(nameError, idError));

        ResponseEntity<ErrorResponseDTO> response = handler.handleValidation(ex, request);

        ErrorResponseDTO body = response.getBody();
        Assertions.assertNotNull(body);
        assertThat(body.getValidationErrors())
                .containsEntry("name", "must not be blank")
                .containsEntry("customerId", "must not be null");
    }

    @Test
    @DisplayName("handleTypeMismatch - returns 400 with parameter name and value in message")
    void handleTypeMismatch_returns400WithParameterDetails() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("sortDir");
        when(ex.getValue()).thenReturn("INVALID");

        ResponseEntity<ErrorResponseDTO> response = handler.handleTypeMismatch(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorResponseDTO body = response.getBody();
        assertCommonFields(body, 400, "Bad Request");
        Assertions.assertNotNull(body);
        assertThat(body.getMessage()).contains("sortDir");
        assertThat(body.getMessage()).contains("INVALID");
    }

    @Test
    @DisplayName("handlePropertyReference - returns 400 with field name in message")
    void handlePropertyReference_returns400WithFieldName() {
        PropertyReferenceException ex =
                new PropertyReferenceException("unknownField", TypeInformation.of(Object.class), List.of());

        ResponseEntity<ErrorResponseDTO> response = handler.handlePropertyReference(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorResponseDTO body = response.getBody();
        assertCommonFields(body, 400, "Bad Request");
        assertThat(body.getMessage()).contains("unknownField");
    }

    @Test
    @DisplayName("handleUnreadable - returns 400 with malformed body message")
    void handleUnreadable_returns400WithMalformedBodyMessage() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);

        ResponseEntity<ErrorResponseDTO> response = handler.handleUnreadable(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorResponseDTO body = response.getBody();
        assertCommonFields(body, 400, "Bad Request");
        assertThat(body.getMessage()).isEqualTo("Malformed or missing body request");
    }

    @Test
    @DisplayName("handleDataAccess - returns 503 with database error message")
    void handleDataAccess_returns503WithDatabaseErrorMessage() {
        DataAccessException ex = mock(DataAccessException.class);

        ResponseEntity<ErrorResponseDTO> response = handler.handleDataAccess(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        ErrorResponseDTO body = response.getBody();
        assertCommonFields(body, 503, "Service Unavailable");
        assertThat(body.getMessage()).isEqualTo("A database error occurred,Please try again");
    }

    @Test
    @DisplayName("handleMethodNotSupported - returns 405 with http method name in message")
    void handleMethodNotSupported_returns405WithMethodName() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("DELETE");

        ResponseEntity<ErrorResponseDTO> response = handler.handleMethodNotSupported(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        ErrorResponseDTO body = response.getBody();
        assertCommonFields(body, 405, "Method Not Allowed");
        Assertions.assertNotNull(body);
        assertThat(body.getMessage()).contains("DELETE");
        assertThat(body.getMessage()).contains("not supported");
    }

    @Test
    @DisplayName("handleNoResource - returns 404 with resource not found message")
    void handleNoResource_returns404WithNotFoundMessage() throws Exception {
        NoResourceFoundException ex =
                new NoResourceFoundException(org.springframework.http.HttpMethod.GET, "/unknown/path");

        ResponseEntity<ErrorResponseDTO> response = handler.handleNoResource(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        ErrorResponseDTO body = response.getBody();
        assertCommonFields(body, 404, "Not Found");
        Assertions.assertNotNull(body);
        assertThat(body.getMessage()).isEqualTo("The requested resource was not found");
    }

    @Test
    @DisplayName("customerNotFound - returns 404 with exception message containing customer id")
    void customerNotFound_returns404WithExceptionMessage() {
        CustomerNotFoundException ex = new CustomerNotFoundException(42L);

        ResponseEntity<ErrorResponseDTO> response = handler.customerNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        ErrorResponseDTO body = response.getBody();
        assertCommonFields(body, 404, "Not Found");
        Assertions.assertNotNull(body);
        assertThat(body.getMessage()).isEqualTo("Customer not found with Id : 42");
    }

    @Test
    @DisplayName("orderNotFound - returns 404 with exception message containing order id")
    void orderNotFound_returns404WithExceptionMessage() {
        OrderNotFoundException ex = new OrderNotFoundException(7L);

        ResponseEntity<ErrorResponseDTO> response = handler.orderNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        ErrorResponseDTO body = response.getBody();
        assertCommonFields(body, 404, "Not Found");
        Assertions.assertNotNull(body);
        assertThat(body.getMessage()).isEqualTo("Order not found with Id: 7");
    }

    @Test
    @DisplayName("productNotFound - returns 404 with single product id in message")
    void productNotFound_singleId_returns404WithExceptionMessage() {
        ProductNotFoundException ex = new ProductNotFoundException(15L);

        ResponseEntity<ErrorResponseDTO> response = handler.productNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        ErrorResponseDTO body = response.getBody();
        assertCommonFields(body, 404, "Not Found");
        Assertions.assertNotNull(body);
        assertThat(body.getMessage()).isEqualTo("Product not found with Id : 15");
    }

    @Test
    @DisplayName("productNotFound - returns 404 with all missing product ids in message (Set variant)")
    void productNotFound_multipleIds_returns404WithAllIdsInMessage() {
        ProductNotFoundException ex = new ProductNotFoundException(Set.of(10L, 20L));

        ResponseEntity<ErrorResponseDTO> response = handler.productNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        ErrorResponseDTO body = response.getBody();
        assertCommonFields(body, 404, "Not Found");
        Assertions.assertNotNull(body);
        assertThat(body.getMessage()).contains("10");
        assertThat(body.getMessage()).contains("20");
    }

    @Test
    @DisplayName("handleGeneric - returns 500 with generic unexpected error message")
    void handleGeneric_returns500WithGenericMessage() {
        Exception ex = new Exception("something internal broke");

        ResponseEntity<ErrorResponseDTO> response = handler.handleGeneric(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ErrorResponseDTO body = response.getBody();
        assertCommonFields(body, 500, "Internal Server Error");
        Assertions.assertNotNull(body);
        assertThat(body.getMessage()).isEqualTo("An unexpected error occurred. Please try again later.");
        assertThat(body.getMessage()).doesNotContain("something internal broke");
    }

    @Test
    @DisplayName("build - response body always has timestamp, status, error, message, and path")
    void responseBody_alwaysHasAllRequiredFields() {
        CustomerNotFoundException ex = new CustomerNotFoundException(1L);

        ResponseEntity<ErrorResponseDTO> response = handler.customerNotFound(ex, request);

        ErrorResponseDTO body = response.getBody();
        Assertions.assertNotNull(body);
        assertThat(body.getTimestamp()).isNotNull();
        assertThat(body.getStatus()).isNotNull();
        assertThat(body.getError()).isNotBlank();
        assertThat(body.getMessage()).isNotBlank();
        assertThat(body.getPath()).isEqualTo("/test-path");
    }
}

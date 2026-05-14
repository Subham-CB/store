package com.example.store.integration;

import com.example.store.api.model.CustomerDTO;
import com.example.store.api.model.CustomerRequestDTO;
import com.example.store.api.model.ErrorResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomerControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private CacheManager cacheManager;

    // Known seeded values from data.sql
    private static final Long   SEEDED_ID        = 1L;
    private static final String SEEDED_NAME      = "Muriel Donnelly";
    private static final String SEEDED_SEARCH    = "Glover";
    private static final int    SEEDED_GLOVER_COUNT = 3;

    @BeforeEach
    void evictCache() {
        // Clear the customers cache before each test
        Cache cache = cacheManager.getCache("customers");
        if (cache != null) {
            cache.clear();
        }
    }

    @Test
    @DisplayName("POST /customer - creates a new customer and returns 201 with id and name")
    void createCustomer_success_returns201(){
        CustomerRequestDTO request = new CustomerRequestDTO();
        request.setName("Test Customer");

        ResponseEntity<CustomerDTO> response = testRestTemplate.postForEntity("/customer",request, CustomerDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isGreaterThanOrEqualTo(101L);
        assertThat(response.getBody().getName()).isEqualTo("Test Customer");
    }

    @Test
    @DisplayName("POST /customer - returns 400 with validation error when name is missing")
    void createCustomer_missingName_returns400WithValidationError() {
        // name is required in CustomerRequestDTO per OpenAPI spec
        CustomerRequestDTO request = new CustomerRequestDTO(); // name not set

        ResponseEntity<ErrorResponseDTO> response =
                testRestTemplate.postForEntity("/customer", request, ErrorResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getValidationErrors()).containsKey("name");
    }

    @Test
    @DisplayName("GET /customer?name=Glover - returns all 3 seeded customers with Glover in name")
    void getCustomers_searchByName_returnsMatchingSeededCustomers() {
        ResponseEntity<CustomerDTO[]> response =
                testRestTemplate.getForEntity("/customer?name=" + SEEDED_SEARCH, CustomerDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(SEEDED_GLOVER_COUNT);
        assertThat(response.getBody())
                .extracting(CustomerDTO::getName)
                .allMatch(name -> name.toLowerCase().contains(SEEDED_SEARCH.toLowerCase()));
    }

    @Test
    @DisplayName("Get /customer - returns first page of 20 customers")
    void getCustomers_getAll_returnsCustomers(){

        ResponseEntity<CustomerDTO[]> response = testRestTemplate
                .getForEntity("/customer",CustomerDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(20);//Default limit is set at 20 for first page
        assertThat(response.getBody())
                .allSatisfy(customer -> {
                    assertThat(customer.getId()).isPositive();
                    assertThat(customer.getName()).isNotNull();
                });
    }

    @Test
    @DisplayName("GET /customer/1 - returns Muriel Donnelly from seeded data")
    void getCustomerById_seededId_returnsCorrectCustomer() {
        ResponseEntity<CustomerDTO> response =
                testRestTemplate.getForEntity("/customer/" + SEEDED_ID, CustomerDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(SEEDED_ID);
        assertThat(response.getBody().getName()).isEqualTo(SEEDED_NAME);
    }

    @Test
    @DisplayName("GET /customer/999999 - returns 404 with message for non-existent id")
    void getCustomerById_nonExistentId_returns404() {
        ResponseEntity<ErrorResponseDTO> response =
                testRestTemplate.getForEntity("/customer/999999", ErrorResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).contains("999999");
    }

    @Test
    @DisplayName("Redis - GET /customer/{id} populates cache on first call, eviction clears it")
    void getCustomerById_cachingAndEviction() {

        Cache cache = cacheManager.getCache("customers");
        assertThat(cache).isNotNull();

        String cacheKey = "id_" + SEEDED_ID;
        assertThat(cache.get(cacheKey)).isNull();

        ResponseEntity<CustomerDTO> firstResponse =
                testRestTemplate.getForEntity("/customer/" + SEEDED_ID, CustomerDTO.class);
        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(cache.get(cacheKey)).isNotNull();

        ResponseEntity<CustomerDTO> secondResponse =
                testRestTemplate.getForEntity("/customer/" + SEEDED_ID, CustomerDTO.class);
        assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(secondResponse.getBody().getName()).isEqualTo(firstResponse.getBody().getName());

        cache.evict(cacheKey);
        assertThat(cache.get(cacheKey)).isNull();
    }

    @Test
    @DisplayName("POST /customer - creating a customer evicts the customers cache")
    void createCustomer_evictsCustomerCache(){

        Cache cache = cacheManager.getCache("customers");
        assertThat(cache).isNotNull();

        String cacheKey = "id_" + SEEDED_ID;

        testRestTemplate.getForEntity("/customer/"+ SEEDED_ID, CustomerDTO.class);
        assertThat(cache.get(cacheKey)).isNotNull();

        CustomerRequestDTO request = new CustomerRequestDTO();
        request.setName("Evict Test Customer");

        testRestTemplate.postForEntity("/customer",request,CustomerDTO.class);

        assertThat(cache.get(cacheKey)).isNull();

    }

}

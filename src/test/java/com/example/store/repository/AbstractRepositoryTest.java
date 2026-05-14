package com.example.store.repository;

import com.example.store.test.config.TestContainersConfig;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
public class AbstractRepositoryTest {

    @BeforeAll
    static void startContainers() {
        TestContainersConfig.POSTGRES.start();
    }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", TestContainersConfig.POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", TestContainersConfig.POSTGRES::getUsername);
        registry.add("spring.datasource.password", TestContainersConfig.POSTGRES::getPassword);
    }
}

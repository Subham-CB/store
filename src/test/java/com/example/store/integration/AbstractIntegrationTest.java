package com.example.store.integration;

import com.example.store.test.config.TestContainersConfig;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.example.store.test.config.TestContainersConfig.POSTGRES;
import static com.example.store.test.config.TestContainersConfig.REDIS;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @BeforeAll
    static void startContainers() {
        TestContainersConfig.POSTGRES.start();
        TestContainersConfig.REDIS.start();
    }



    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry){

        registry.add("spring.datasource.url",POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username",POSTGRES::getUsername);
        registry.add("spring.datasource.password",POSTGRES::getPassword);

        registry.add("spring.data.redis.host",REDIS::getHost);
        registry.add("spring.data.redis.port", REDIS::getFirstMappedPort);

    }
}

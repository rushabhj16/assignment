package com.assignment.backend;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("acceptance")
@SpringBootTest
class BackendApplicationIntegrationTest {

    @Test
    void contextLoads() {
        assertTrue(true);
    }
}

package com.sebo.livros;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "sebo.seed.enabled=false")
class LivrosApplicationTests {

    @Test
    void contextLoads() {
    }
}
package com.oracle.graalvm.demo;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PriceControllerTest {

    @Autowired
    private TestRestTemplate template;

    @Test
    public void getIndex() throws Exception {
        ResponseEntity<String> response = template.getForEntity("/", String.class);
        assertThat(response.getBody()).isEqualTo("Welcome to GraalVM Spring Boot HOL!\n");
    }

    @Test
    public void getPrice() throws Exception {
        ResponseEntity<String> response = template.getForEntity("/price/GRAAL_PREMIUM_BOND/100/1?yield=0.01&interestRate=0.05", String.class);
        assertThat(response.getBody().toString().contains(("103,960")));
    }

}
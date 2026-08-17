package com.sentinel.gateway.integration;


import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "ORDER_SERVICE_URL=http://localhost:8080"
        }
)
@AutoConfigureTestRestTemplate
class GatewayRoutingTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private WireMockServer orderServiceMock;

    @BeforeEach
    void startMockOrderService() {
        orderServiceMock = new WireMockServer(8080);
        orderServiceMock.start();
        configureFor("localhost", 8080);
    }

    @AfterEach
    void stopMockOrderService() {
        if (orderServiceMock != null) {
            orderServiceMock.stop();
        }
    }

    @Test
    void orderPathIsForwardedToOtherService() {
        stubFor(get(urlEqualTo("/orders/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"orderId\":1,\"status\":\"PENDING\"}"))
        );

        ResponseEntity<String> response = restTemplate.getForEntity("/orders/1", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("PENDING");

        verify(getRequestedFor(urlEqualTo("/orders/1")));
    }

    @Test
    void unmatchedPathReturnsGatewayNotFound() {
        ResponseEntity<String> response = restTemplate.getForEntity("/nonexistent-path", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}

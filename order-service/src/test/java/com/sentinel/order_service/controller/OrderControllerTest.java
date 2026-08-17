package com.sentinel.order_service.controller;

import com.sentinel.order_service.exception.OrderNotFoundException;
import com.sentinel.order_service.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void createOrder_missingCustomerId_returns400() throws Exception {
        String body = """
                {
                  "items": [
                    { "sku": "WIDGET-1", "quantity": 1 }
                  ]
                }
                """;

        mockMvc.perform(post("/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrder_emptyItems_return400() throws Exception {
        String body = """
                {
                  "customerId": 1,
                  "items": []
                }
                """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrder_negativeQuantity_returns400() throws Exception {
        String body = """
                {
                  "customerId": 1,
                  "items": [
                    { "sku": "WIDGET-1", "quantity": -5 }
                  ]
                }
                """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrder_blankSku_returns400() throws Exception {
        String body = """
                {
                  "customerId": 1,
                  "items": [
                    { "sku": "", "quantity": 1 }
                  ]
                }
                """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrder_duplicateSkus_returns400() throws Exception {
        String body = """
                {
                  "customerId": 1,
                  "items": [
                    { "sku": "WIDGET-1", "quantity": 1 },
                    { "sku": "WIDGET-1", "quantity": 2 }
                  ]
                }
                """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOrder_notFound_returns404() throws Exception {
        when(orderService.getOrderById(999L)).thenThrow(new OrderNotFoundException(999L));

        mockMvc.perform(get("/orders/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOrder_nonNumericId_return400() throws Exception {
        mockMvc.perform(get("/orders/abc"))
                .andExpect(status().isBadRequest());
    }
}

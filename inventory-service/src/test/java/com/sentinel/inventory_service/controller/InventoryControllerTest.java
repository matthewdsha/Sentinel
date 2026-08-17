package com.sentinel.inventory_service.controller;

import com.sentinel.inventory_service.exception.InventoryNotFoundException;
import com.sentinel.inventory_service.service.InventoryService;
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

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventoryService inventoryService;

    @Test
    void addStock_blankSku_returns400() throws Exception {
        String body = """
                { "sku": "", "quantity": 10 }
                """;

        mockMvc.perform(post("/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addStock_missingQuantity_return400() throws  Exception {
        String body = """
                { "sku": "WIDGET-1" }
                """;

        mockMvc.perform(post("/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addStock_zeroQuantity_return400() throws Exception {
        String body = """
                { "sku": "WIDGET-1", "quantity": 0 }
                """;

        mockMvc.perform(post("/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getInventory_notFound_returns404() throws Exception {
        when(inventoryService.getInventory("GHOST-SKU")).thenThrow(new InventoryNotFoundException("GHOST-SKU"));

        mockMvc.perform(get("/inventory/GHOST-SKU"))
                .andExpect(status().isNotFound());
    }
}

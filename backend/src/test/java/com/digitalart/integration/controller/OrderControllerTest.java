package com.digitalart.integration.controller;

import com.digitalart.order.application.OrderService;
import com.digitalart.order.application.dto.CreateOrderRequest;
import com.digitalart.order.application.dto.OrderDto;
import com.digitalart.shared.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderDto createOrderDto(Long id) {
        return OrderDto.builder()
                .id(id).userId(1L).artworkId(1L)
                .artworkTitle("Test Artwork")
                .status("PENDING")
                .totalPrice(new BigDecimal("100.00"))
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser
    void createOrder_shouldReturn201() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setArtworkId(1L);

        when(orderService.createOrder(any(CreateOrderRequest.class), anyString()))
                .thenReturn(createOrderDto(1L));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser
    void createOrder_whenSold_shouldReturn400() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setArtworkId(1L);

        when(orderService.createOrder(any(CreateOrderRequest.class), anyString()))
                .thenThrow(new BusinessException("This artwork is already sold"));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getMyOrders_shouldReturnList() throws Exception {
        when(orderService.getMyOrders(anyString())).thenReturn(List.of(
                createOrderDto(1L), createOrderDto(2L)
        ));

        mockMvc.perform(get("/api/orders/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser
    void getOrderById_shouldReturnOrder() throws Exception {
        when(orderService.getOrderById(eq(1L), anyString())).thenReturn(createOrderDto(1L));

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}

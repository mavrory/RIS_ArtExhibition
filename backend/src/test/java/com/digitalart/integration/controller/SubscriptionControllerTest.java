package com.digitalart.integration.controller;

import com.digitalart.subscription.application.SubscriptionDTO;
import com.digitalart.subscription.application.SubscriptionService;
import com.digitalart.user.application.UserService;
import com.digitalart.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SubscriptionService subscriptionService;

    @MockBean
    private UserService userService;

    private User createUser() {
        return User.builder().id(1L).email("user@example.com").username("user").build();
    }

    private SubscriptionDTO createDto(Long id) {
        return new SubscriptionDTO(id, 1L, "subscriber", 2L, "artist", LocalDateTime.now());
    }

    @Test
    @WithMockUser
    void subscribe_shouldSucceed() throws Exception {
        when(userService.getUserByEmail(anyString())).thenReturn(createUser());
        when(subscriptionService.subscribe(eq(1L), eq(2L))).thenReturn(createDto(1L));

        mockMvc.perform(post("/api/subscriptions/2"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void subscribe_toSelf_shouldReturnError() throws Exception {
        when(userService.getUserByEmail(anyString())).thenReturn(createUser());
        when(subscriptionService.subscribe(eq(1L), eq(1L)))
                .thenThrow(new IllegalArgumentException("Cannot subscribe to yourself"));

        mockMvc.perform(post("/api/subscriptions/1"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser
    void unsubscribe_shouldSucceed() throws Exception {
        when(userService.getUserByEmail(anyString())).thenReturn(createUser());

        mockMvc.perform(delete("/api/subscriptions/2"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getMySubscriptions_shouldReturnList() throws Exception {
        when(userService.getUserByEmail(anyString())).thenReturn(createUser());
        when(subscriptionService.getSubscriptionsBySubscriber(1L))
                .thenReturn(List.of(createDto(1L), createDto(2L)));

        mockMvc.perform(get("/api/subscriptions/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser
    void getSubscriberCount_shouldReturnCount() throws Exception {
        when(subscriptionService.getSubscriberCount(2L)).thenReturn(5L);

        mockMvc.perform(get("/api/subscriptions/artist/2/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(5));
    }

    @Test
    @WithMockUser
    void checkSubscription_shouldReturnBoolean() throws Exception {
        when(userService.getUserByEmail(anyString())).thenReturn(createUser());
        when(subscriptionService.isSubscribed(1L, 2L)).thenReturn(true);

        mockMvc.perform(get("/api/subscriptions/check/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSubscribed").value(true));
    }
}

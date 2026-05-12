package com.digitalart.integration.controller;

import com.digitalart.user.application.UserService;
import com.digitalart.user.application.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser
    void getMe_shouldReturnUser() throws Exception {
        UserDto dto = UserDto.builder()
                .id(1L).email("test@example.com")
                .username("testuser").balance(100.0)
                .roles(Set.of("USER"))
                .build();

        when(userService.getCurrentUser(anyString())).thenReturn(dto);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @WithMockUser
    void upgradeToArtist_shouldSucceed() throws Exception {
        mockMvc.perform(post("/api/users/upgrade-to-artist"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void topUpBalance_shouldSucceed() throws Exception {
        UserDto dto = UserDto.builder()
                .id(1L).email("test@example.com")
                .username("testuser").balance(200.0)
                .roles(Set.of("USER"))
                .build();

        when(userService.topUpBalance(anyString(), eq(100.0))).thenReturn(dto);

        mockMvc.perform(post("/api/users/top-up")
                        .param("amount", "100.0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(200.0));
    }

    @Test
    @WithMockUser
    void getUserProfile_shouldReturnProfile() throws Exception {
        UserDto dto = UserDto.builder()
                .id(1L).email("user@test.com")
                .username("user").balance(0.0)
                .roles(Set.of("USER"))
                .build();

        when(userService.getUserDtoById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/users/1/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user"));
    }
}

package com.digitalart.user.application;

import com.digitalart.shared.exception.BusinessException;
import com.digitalart.shared.exception.UnauthorizedException;
import com.digitalart.user.application.dto.AuthResponse;
import com.digitalart.user.application.dto.LoginRequest;
import com.digitalart.user.application.dto.RegisterRequest;
import com.digitalart.user.domain.Role;
import com.digitalart.user.domain.User;
import com.digitalart.user.infrastructure.JwtTokenProvider;
import com.digitalart.user.infrastructure.RoleRepository;
import com.digitalart.user.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldSucceed() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setUsername("testuser");
        request.setIsArtist(false);

        Role userRole = new Role();
        userRole.setName("USER");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

        User savedUser = User.builder()
                .id(1L)
                .email(request.getEmail())
                .username(request.getUsername())
                .roles(Set.of(userRole))
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtTokenProvider.generateToken(savedUser.getEmail())).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertNotNull(response.getUser());
        assertEquals("test@example.com", response.getUser().getEmail());
        assertEquals("testuser", response.getUser().getUsername());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_withArtistRole_shouldAddArtistRole() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("artist@example.com");
        request.setPassword("password123");
        request.setUsername("artistuser");
        request.setIsArtist(true);

        Role userRole = new Role();
        userRole.setName("USER");
        Role artistRole = new Role();
        artistRole.setName("ARTIST");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(roleRepository.findByName("ARTIST")).thenReturn(Optional.of(artistRole));
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded");

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        roles.add(artistRole);

        User savedUser = User.builder()
                .id(2L)
                .email(request.getEmail())
                .username(request.getUsername())
                .roles(roles)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtTokenProvider.generateToken(savedUser.getEmail())).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertTrue(response.getUser().getRoles().contains("ARTIST"));
        assertTrue(response.getUser().getRoles().contains("USER"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User captured = userCaptor.getValue();
        assertTrue(captured.getRoles().stream().anyMatch(r -> r.getName().equals("ARTIST")));
    }

    @Test
    void register_withDuplicateEmail_shouldThrowException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setPassword("password123");
        request.setUsername("existinguser");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(BusinessException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_shouldSucceed() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        Role userRole = new Role();
        userRole.setName("USER");

        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("encodedPassword")
                .username("testuser")
                .roles(Set.of(userRole))
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPasswordHash())).thenReturn(true);
        when(jwtTokenProvider.generateToken(user.getEmail())).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("test@example.com", response.getUser().getEmail());
    }

    @Test
    void login_withInvalidEmail_shouldThrowException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void login_withInvalidPassword_shouldThrowException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongpassword");

        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("encodedPassword")
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPasswordHash())).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }
}

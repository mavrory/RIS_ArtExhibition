package com.digitalart.user.application;

import com.digitalart.shared.exception.ResourceNotFoundException;
import com.digitalart.user.application.dto.UserDto;
import com.digitalart.user.domain.Role;
import com.digitalart.user.domain.User;
import com.digitalart.user.infrastructure.RoleRepository;
import com.digitalart.user.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserService userService;

    private User createTestUser() {
        Role userRole = new Role();
        userRole.setName("USER");
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        return User.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .balance(100.0)
                .roles(roles)
                .build();
    }

    @Test
    void getCurrentUser_shouldReturnUserDto() {
        User user = createTestUser();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        UserDto result = userService.getCurrentUser("test@example.com");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("testuser", result.getUsername());
        assertEquals(100.0, result.getBalance());
    }

    @Test
    void getCurrentUser_withUnknownEmail_shouldThrowException() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getCurrentUser("unknown@example.com"));
    }

    @Test
    void getUserByEmail_shouldReturnUser() {
        User user = createTestUser();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        User result = userService.getUserByEmail("test@example.com");

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getUserByEmail_withUnknownEmail_shouldThrowException() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserByEmail("unknown@example.com"));
    }

    @Test
    void getUserById_shouldReturnUser() {
        User user = createTestUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void getUserById_withUnknownId_shouldThrowException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(999L));
    }

    @Test
    void upgradeToArtist_shouldAddArtistRole() {
        User user = createTestUser();
        Role artistRole = new Role();
        artistRole.setName("ARTIST");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(roleRepository.findByName("ARTIST")).thenReturn(Optional.of(artistRole));

        userService.upgradeToArtist("test@example.com");

        assertTrue(user.getRoles().stream().anyMatch(r -> r.getName().equals("ARTIST")));
        verify(userRepository).save(user);
    }

    @Test
    void topUpBalance_shouldIncreaseBalance() {
        User user = createTestUser();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.topUpBalance("test@example.com", 50.0);

        assertEquals(150.0, result.getBalance());
    }

    @Test
    void getUserDtoById_shouldReturnDto() {
        User user = createTestUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto result = userService.getUserDtoById(1L);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }
}

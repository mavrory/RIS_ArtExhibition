package com.digitalart.user.presentation;

import com.digitalart.user.application.UserService;
import com.digitalart.user.application.dto.UserDto;
import com.digitalart.user.domain.Role;
import com.digitalart.user.domain.User;
import com.digitalart.user.infrastructure.RoleRepository;
import com.digitalart.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        UserDto user = userService.getCurrentUser(email);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/upgrade-to-artist")
    public ResponseEntity<Void> upgradeToArtist(Authentication authentication) {
        String email = authentication.getName();
        userService.upgradeToArtist(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/top-up")
    public ResponseEntity<UserDto> topUpBalance(
            @RequestParam Double amount,
            Authentication authentication) {
        String email = authentication.getName();
        UserDto user = userService.topUpBalance(email, amount);
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserDto> getUserProfile(@PathVariable Long userId) {
        UserDto user = userService.getUserDtoById(userId);
        return ResponseEntity.ok(user);
    }
}

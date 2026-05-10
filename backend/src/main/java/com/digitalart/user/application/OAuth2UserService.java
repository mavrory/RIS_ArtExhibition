package com.digitalart.user.application;

import com.digitalart.user.domain.Role;
import com.digitalart.user.domain.User;
import com.digitalart.user.domain.UserAuthProvider;
import com.digitalart.user.infrastructure.RoleRepository;
import com.digitalart.user.infrastructure.UserAuthProviderRepository;
import com.digitalart.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserAuthProviderRepository authProviderRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            OAuth2User oauth2User = super.loadUser(userRequest);
            
            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            Map<String, Object> attributes = oauth2User.getAttributes();
            
            log.info("OAuth2 login attempt with provider: {}", registrationId);
            
            // Extract user info based on provider
            String email = extractEmail(registrationId, attributes);
            String name = extractName(registrationId, attributes);
            String providerId = extractProviderId(registrationId, attributes);
            
            log.info("Extracted user info - email: {}, name: {}", email, name);
            
            // Find or create user
            User user = findOrCreateUser(email, name, registrationId, providerId);
            
            log.info("User loaded successfully: {}", user.getEmail());
            
            return oauth2User;
        } catch (Exception e) {
            log.error("Error during OAuth2 authentication", e);
            throw new OAuth2AuthenticationException("OAuth2 authentication failed: " + e.getMessage());
        }
    }

    private String extractEmail(String provider, Map<String, Object> attributes) {
        if ("google".equals(provider)) {
            return (String) attributes.get("email");
        } else if ("github".equals(provider)) {
            return (String) attributes.get("email");
        }
        throw new OAuth2AuthenticationException("Unsupported OAuth2 provider: " + provider);
    }

    private String extractName(String provider, Map<String, Object> attributes) {
        if ("google".equals(provider)) {
            return (String) attributes.get("name");
        } else if ("github".equals(provider)) {
            return (String) attributes.get("login");
        }
        return "Unknown";
    }

    private String extractProviderId(String provider, Map<String, Object> attributes) {
        if ("google".equals(provider)) {
            return (String) attributes.get("sub");
        } else if ("github".equals(provider)) {
            return String.valueOf(attributes.get("id"));
        }
        throw new OAuth2AuthenticationException("Unsupported OAuth2 provider: " + provider);
    }

    private User findOrCreateUser(String email, String name, String provider, String providerId) {
        UserAuthProvider.AuthProvider authProvider = UserAuthProvider.AuthProvider.valueOf(provider.toUpperCase());
        
        log.info("Finding or creating user for email: {}, provider: {}", email, provider);
        
        // Check if user already exists with this OAuth provider
        return authProviderRepository.findByProviderAndProviderUserId(authProvider, providerId)
                .map(userAuthProvider -> {
                    log.info("Found existing OAuth provider link for user: {}", userAuthProvider.getUser().getEmail());
                    return userAuthProvider.getUser();
                })
                .orElseGet(() -> {
                    // Check if user exists by email
                    User user = userRepository.findByEmail(email)
                            .map(existingUser -> {
                                log.info("Found existing user by email: {}", existingUser.getEmail());
                                return existingUser;
                            })
                            .orElseGet(() -> {
                                log.info("Creating new user for email: {}", email);
                                return createNewUser(email, name);
                            });
                    
                    // Link OAuth provider to user
                    UserAuthProvider userAuthProvider = UserAuthProvider.builder()
                            .user(user)
                            .provider(authProvider)
                            .providerUserId(providerId)
                            .build();
                    authProviderRepository.save(userAuthProvider);
                    log.info("Linked OAuth provider {} to user {}", provider, user.getEmail());
                    
                    return user;
                });
    }

    private User createNewUser(String email, String name) {
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("USER role not found"));
        
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        
        User user = User.builder()
                .email(email)
                .username(name)
                .passwordHash("") // No password for OAuth users
                .balance(0.0)
                .roles(roles)
                .build();
        
        User savedUser = userRepository.save(user);
        log.info("Created new user with id: {}", savedUser.getId());
        return savedUser;
    }
}

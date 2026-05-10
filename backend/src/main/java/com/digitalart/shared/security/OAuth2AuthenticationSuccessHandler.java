package com.digitalart.shared.security;

import com.digitalart.user.domain.User;
import com.digitalart.user.infrastructure.JwtTokenProvider;
import com.digitalart.user.infrastructure.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        
        log.info("OAuth2 authentication successful for email: {}", email);
        
        // Генерируем JWT токен для пользователя
        String token = jwtTokenProvider.generateToken(email);
        
        // Проверяем, является ли пользователь новым
        Optional<User> userOpt = userRepository.findByEmail(email);
        boolean needsRoleSelection = false;
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Показываем выбор роли только для новых пользователей (созданных менее 10 секунд назад)
            // Это означает, что пользователь только что зарегистрировался через OAuth2
            if (user.getCreatedAt().isAfter(java.time.LocalDateTime.now().minusSeconds(10))) {
                needsRoleSelection = true;
                log.info("New user detected, redirecting to role selection");
            } else {
                log.info("Existing user, redirecting to callback");
            }
        }
        
        String targetUrl;
        if (needsRoleSelection) {
            targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth2/role-selection")
                    .queryParam("token", token)
                    .build().toUriString();
        } else {
            targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth2/callback")
                    .queryParam("token", token)
                    .build().toUriString();
        }
        
        log.info("Redirecting to: {}", targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}

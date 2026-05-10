package com.digitalart.subscription.presentation;

import com.digitalart.subscription.application.SubscriptionDTO;
import com.digitalart.subscription.application.SubscriptionService;
import com.digitalart.user.application.UserService;
import com.digitalart.user.domain.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {
    
    private final SubscriptionService subscriptionService;
    private final UserService userService;
    
    public SubscriptionController(SubscriptionService subscriptionService, UserService userService) {
        this.subscriptionService = subscriptionService;
        this.userService = userService;
    }
    
    @PostMapping("/{artistId}")
    public ResponseEntity<?> subscribe(
            @PathVariable Long artistId) {
        try {
            System.out.println("=== Subscribe request ===");
            
            // Get authentication from SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Authentication from SecurityContext: " + authentication);
            
            if (authentication == null || !authentication.isAuthenticated()) {
                System.err.println("ERROR: User not authenticated");
                return ResponseEntity.status(401).body("User not authenticated");
            }
            
            String email = authentication.getName();
            System.out.println("User email from authentication: " + email);
            
            if (email == null || email.equals("anonymousUser")) {
                System.err.println("ERROR: Anonymous user");
                return ResponseEntity.status(401).body("Anonymous user");
            }
            
            User user = userService.getUserByEmail(email);
            
            if (user == null) {
                System.err.println("ERROR: User is null after getUserByEmail");
                return ResponseEntity.status(404).body("User not found");
            }
            
            System.out.println("User found: " + user.getUsername() + " (ID: " + user.getId() + ")");
            
            SubscriptionDTO subscription = subscriptionService.subscribe(user.getId(), artistId);
            System.out.println("Subscription created successfully");
            
            return ResponseEntity.ok(subscription);
        } catch (Exception e) {
            System.err.println("ERROR in subscribe: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{artistId}")
    public ResponseEntity<?> unsubscribe(
            @PathVariable Long artistId) {
        try {
            System.out.println("=== Unsubscribe request ===");
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                System.err.println("ERROR: User not authenticated");
                return ResponseEntity.status(401).body("User not authenticated");
            }
            
            String email = authentication.getName();
            System.out.println("User email: " + email);
            
            if (email == null || email.equals("anonymousUser")) {
                System.err.println("ERROR: Anonymous user");
                return ResponseEntity.status(401).body("Anonymous user");
            }
            
            User user = userService.getUserByEmail(email);
            
            if (user == null) {
                System.err.println("ERROR: User is null");
                return ResponseEntity.status(404).body("User not found");
            }
            
            System.out.println("User found: " + user.getUsername() + " (ID: " + user.getId() + ")");
            
            subscriptionService.unsubscribe(user.getId(), artistId);
            System.out.println("Unsubscribed successfully");
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("ERROR in unsubscribe: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping("/my")
    public ResponseEntity<?> getMySubscriptions() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body("User not authenticated");
            }
            
            String email = authentication.getName();
            User user = userService.getUserByEmail(email);
            List<SubscriptionDTO> subscriptions = subscriptionService.getSubscriptionsBySubscriber(user.getId());
            return ResponseEntity.ok(subscriptions);
        } catch (Exception e) {
            System.err.println("ERROR in getMySubscriptions: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping("/artist/{artistId}")
    public ResponseEntity<List<SubscriptionDTO>> getArtistSubscribers(
            @PathVariable Long artistId) {
        List<SubscriptionDTO> subscribers = subscriptionService.getSubscribersByArtist(artistId);
        return ResponseEntity.ok(subscribers);
    }
    
    @GetMapping("/artist/{artistId}/count")
    public ResponseEntity<Map<String, Long>> getSubscriberCount(
            @PathVariable Long artistId) {
        Long count = subscriptionService.getSubscriberCount(artistId);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/check/{artistId}")
    public ResponseEntity<Map<String, Boolean>> checkSubscription(
            Authentication authentication,
            @PathVariable Long artistId) {
        if (authentication == null) {
            Map<String, Boolean> response = new HashMap<>();
            response.put("isSubscribed", false);
            return ResponseEntity.ok(response);
        }
        
        String email = authentication.getName();
        User user = userService.getUserByEmail(email);
        boolean isSubscribed = subscriptionService.isSubscribed(user.getId(), artistId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isSubscribed", isSubscribed);
        return ResponseEntity.ok(response);
    }
}

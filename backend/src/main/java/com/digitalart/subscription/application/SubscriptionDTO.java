package com.digitalart.subscription.application;

import java.time.LocalDateTime;

public class SubscriptionDTO {
    
    private Long id;
    private Long subscriberId;
    private String subscriberUsername;
    private Long artistId;
    private String artistUsername;
    private LocalDateTime createdAt;
    
    public SubscriptionDTO() {}
    
    public SubscriptionDTO(Long id, Long subscriberId, String subscriberUsername, 
                          Long artistId, String artistUsername, LocalDateTime createdAt) {
        this.id = id;
        this.subscriberId = subscriberId;
        this.subscriberUsername = subscriberUsername;
        this.artistId = artistId;
        this.artistUsername = artistUsername;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getSubscriberId() {
        return subscriberId;
    }
    
    public void setSubscriberId(Long subscriberId) {
        this.subscriberId = subscriberId;
    }
    
    public String getSubscriberUsername() {
        return subscriberUsername;
    }
    
    public void setSubscriberUsername(String subscriberUsername) {
        this.subscriberUsername = subscriberUsername;
    }
    
    public Long getArtistId() {
        return artistId;
    }
    
    public void setArtistId(Long artistId) {
        this.artistId = artistId;
    }
    
    public String getArtistUsername() {
        return artistUsername;
    }
    
    public void setArtistUsername(String artistUsername) {
        this.artistUsername = artistUsername;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

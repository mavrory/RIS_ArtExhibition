package com.digitalart.subscription.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"subscriber_id", "artist_id"})
})
public class Subscription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "subscriber_id", nullable = false)
    private Long subscriberId;
    
    @Column(name = "artist_id", nullable = false)
    private Long artistId;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    public Subscription() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Subscription(Long subscriberId, Long artistId) {
        this.subscriberId = subscriberId;
        this.artistId = artistId;
        this.createdAt = LocalDateTime.now();
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
    
    public Long getArtistId() {
        return artistId;
    }
    
    public void setArtistId(Long artistId) {
        this.artistId = artistId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

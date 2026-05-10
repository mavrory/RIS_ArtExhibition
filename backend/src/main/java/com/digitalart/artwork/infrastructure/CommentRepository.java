package com.digitalart.artwork.infrastructure;

import com.digitalart.artwork.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    List<Comment> findByArtworkIdOrderByCreatedAtDesc(Long artworkId);
}

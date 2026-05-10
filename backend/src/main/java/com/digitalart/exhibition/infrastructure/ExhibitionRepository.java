package com.digitalart.exhibition.infrastructure;

import com.digitalart.exhibition.domain.Exhibition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExhibitionRepository extends JpaRepository<Exhibition, Long> {
    
    List<Exhibition> findByCreatedBy(Long createdBy);
}

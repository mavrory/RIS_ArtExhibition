package com.digitalart.exhibition.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExhibitionDto {

    private Long id;
    private String title;
    private String description;
    private Long createdBy;
    private String creatorName;
    private Integer artworksCount;
    private String coverImageUrl;
    private LocalDateTime createdAt;
}

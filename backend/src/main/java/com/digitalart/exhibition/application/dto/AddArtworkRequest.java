package com.digitalart.exhibition.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddArtworkRequest {

    @NotNull(message = "Artwork ID is required")
    private Long artworkId;
}

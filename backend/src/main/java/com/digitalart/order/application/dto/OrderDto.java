package com.digitalart.order.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {

    private Long id;
    private Long userId;
    private Long artworkId;
    private String artworkTitle;
    private String status;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
}

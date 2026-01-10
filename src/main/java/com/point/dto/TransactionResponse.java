package com.point.dto;

import com.point.domain.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private String pointKey;
    private TransactionType type;
    private Long amount;
    private String orderId;
    private Long cancelledAmount;
    private LocalDateTime createdAt;
}

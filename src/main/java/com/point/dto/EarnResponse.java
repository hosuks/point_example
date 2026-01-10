package com.point.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EarnResponse {

    private String pointKey;
    private Long memberId;
    private Long amount;
    private boolean manual;
    private LocalDateTime expirationDate;
    private Long balance;
}

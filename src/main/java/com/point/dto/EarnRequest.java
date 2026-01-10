package com.point.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EarnRequest {

    @NotNull(message = "회원 ID는 필수입니다")
    private Long memberId;

    @NotNull(message = "적립 금액은 필수입니다")
    @Positive(message = "적립 금액은 양수여야 합니다")
    private Long amount;

    @Builder.Default
    private boolean manual = false;

    private Integer expiryDays;
}

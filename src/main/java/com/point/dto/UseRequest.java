package com.point.dto;

import jakarta.validation.constraints.NotBlank;
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
public class UseRequest {

    @NotNull(message = "회원 ID는 필수입니다")
    private Long memberId;

    @NotNull(message = "사용 금액은 필수입니다")
    @Positive(message = "사용 금액은 양수여야 합니다")
    private Long amount;

    @NotBlank(message = "주문번호는 필수입니다")
    private String orderId;
}

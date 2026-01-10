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
public class CancelUseRequest {

    @NotBlank(message = "포인트 키는 필수입니다")
    private String pointKey;

    @NotNull(message = "취소 금액은 필수입니다")
    @Positive(message = "취소 금액은 양수여야 합니다")
    private Long amount;
}

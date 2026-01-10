package com.point.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceResponse {

    private Long memberId;
    private Long balance;
    private List<PointDetailDto> points;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PointDetailDto {
        private Long pointId;
        private Long originalAmount;
        private Long remainingAmount;
        private boolean manual;
        private LocalDateTime expirationDate;
    }
}

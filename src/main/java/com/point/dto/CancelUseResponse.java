package com.point.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelUseResponse {

    private String pointKey;
    private String originalPointKey;
    private Long memberId;
    private Long cancelledAmount;
    private Long remainingCancellableAmount;
    private Long balance;
    private List<CancelDetailDto> cancelDetails;
    private List<NewEarnDto> newEarns;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CancelDetailDto {
        private Long originalPointId;
        private Long cancelledAmount;
        private boolean expired;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewEarnDto {
        private String pointKey;
        private Long amount;
        private String reason;
    }
}

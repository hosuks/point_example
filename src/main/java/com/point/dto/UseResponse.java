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
public class UseResponse {

    private String pointKey;
    private Long memberId;
    private Long usedAmount;
    private String orderId;
    private Long balance;
    private List<UsageDetailDto> usageDetails;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsageDetailDto {
        private Long pointId;
        private Long usedAmount;
    }
}

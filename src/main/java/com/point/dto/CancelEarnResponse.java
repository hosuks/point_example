package com.point.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelEarnResponse {

    private String pointKey;
    private String originalPointKey;
    private Long memberId;
    private Long cancelledAmount;
    private Long balance;
}

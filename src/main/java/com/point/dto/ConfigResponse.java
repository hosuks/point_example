package com.point.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigResponse {

    private Long id;
    private String configKey;
    private String configValue;
    private String description;
}

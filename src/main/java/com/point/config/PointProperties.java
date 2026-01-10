package com.point.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "point")
@Getter
@Setter
public class PointProperties {

    private Long maxEarnAmount = 100000L;
    private Long minEarnAmount = 1L;
    private Long maxBalancePerMember = 1000000L;
    private Integer defaultExpiryDays = 365;
    private Integer minExpiryDays = 1;
    private Integer maxExpiryDays = 1825;
}

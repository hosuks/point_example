package com.point.service;

import com.point.config.PointConfigKey;
import com.point.config.PointProperties;
import com.point.domain.PointConfig;
import com.point.repository.PointConfigRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointConfigService {

    private final PointConfigRepository pointConfigRepository;
    private final PointProperties pointProperties;

    @PostConstruct
    @Transactional
    public void initializeDefaultConfigs() {
        createConfigIfNotExists(PointConfigKey.MAX_EARN_AMOUNT,
                String.valueOf(pointProperties.getMaxEarnAmount()),
                "1회 최대 적립 가능 포인트");

        createConfigIfNotExists(PointConfigKey.MIN_EARN_AMOUNT,
                String.valueOf(pointProperties.getMinEarnAmount()),
                "1회 최소 적립 가능 포인트");

        createConfigIfNotExists(PointConfigKey.MAX_BALANCE_PER_MEMBER,
                String.valueOf(pointProperties.getMaxBalancePerMember()),
                "개인별 최대 보유 가능 포인트");

        createConfigIfNotExists(PointConfigKey.DEFAULT_EXPIRY_DAYS,
                String.valueOf(pointProperties.getDefaultExpiryDays()),
                "기본 만료일 (일)");

        createConfigIfNotExists(PointConfigKey.MIN_EXPIRY_DAYS,
                String.valueOf(pointProperties.getMinExpiryDays()),
                "최소 만료일 (일)");

        createConfigIfNotExists(PointConfigKey.MAX_EXPIRY_DAYS,
                String.valueOf(pointProperties.getMaxExpiryDays()),
                "최대 만료일 (5년 미만)");
    }

    private void createConfigIfNotExists(String key, String value, String description) {
        if (pointConfigRepository.findByConfigKey(key).isEmpty()) {
            pointConfigRepository.save(new PointConfig(key, value, description));
        }
    }

    @Transactional(readOnly = true)
    public Long getMaxEarnAmount() {
        return getConfigValue(PointConfigKey.MAX_EARN_AMOUNT, pointProperties.getMaxEarnAmount());
    }

    @Transactional(readOnly = true)
    public Long getMinEarnAmount() {
        return getConfigValue(PointConfigKey.MIN_EARN_AMOUNT, pointProperties.getMinEarnAmount());
    }

    @Transactional(readOnly = true)
    public Long getMaxBalancePerMember() {
        return getConfigValue(PointConfigKey.MAX_BALANCE_PER_MEMBER, pointProperties.getMaxBalancePerMember());
    }

    @Transactional(readOnly = true)
    public Integer getDefaultExpiryDays() {
        return getConfigValueInt(PointConfigKey.DEFAULT_EXPIRY_DAYS, pointProperties.getDefaultExpiryDays());
    }

    @Transactional(readOnly = true)
    public Integer getMinExpiryDays() {
        return getConfigValueInt(PointConfigKey.MIN_EXPIRY_DAYS, pointProperties.getMinExpiryDays());
    }

    @Transactional(readOnly = true)
    public Integer getMaxExpiryDays() {
        return getConfigValueInt(PointConfigKey.MAX_EXPIRY_DAYS, pointProperties.getMaxExpiryDays());
    }

    private Long getConfigValue(String key, Long defaultValue) {
        return pointConfigRepository.findByConfigKey(key)
                .map(PointConfig::getValueAsLong)
                .orElse(defaultValue);
    }

    private Integer getConfigValueInt(String key, Integer defaultValue) {
        return pointConfigRepository.findByConfigKey(key)
                .map(PointConfig::getValueAsInt)
                .orElse(defaultValue);
    }

    @Transactional
    public PointConfig updateConfig(String key, String value) {
        PointConfig config = pointConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new IllegalArgumentException("Config not found: " + key));
        config.updateValue(value);
        return pointConfigRepository.save(config);
    }

    @Transactional(readOnly = true)
    public List<PointConfig> getAllConfigs() {
        return pointConfigRepository.findAll();
    }
}

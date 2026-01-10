package com.point.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "point_configs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String configKey;

    @Column(nullable = false)
    private String configValue;

    private String description;

    public PointConfig(String configKey, String configValue, String description) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.description = description;
    }

    public void updateValue(String configValue) {
        this.configValue = configValue;
    }

    public Long getValueAsLong() {
        return Long.parseLong(this.configValue);
    }

    public Integer getValueAsInt() {
        return Integer.parseInt(this.configValue);
    }
}

package com.point.controller;

import com.point.domain.PointConfig;
import com.point.dto.ConfigResponse;
import com.point.dto.ConfigUpdateRequest;
import com.point.service.PointConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/configs")
@RequiredArgsConstructor
public class ConfigController {

    private final PointConfigService configService;

    @GetMapping
    public ResponseEntity<List<ConfigResponse>> getAllConfigs() {
        List<ConfigResponse> configs = configService.getAllConfigs().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(configs);
    }

    @PutMapping("/{key}")
    public ResponseEntity<ConfigResponse> updateConfig(
            @PathVariable String key,
            @Valid @RequestBody ConfigUpdateRequest request) {
        PointConfig config = configService.updateConfig(key, request.getValue());
        return ResponseEntity.ok(toResponse(config));
    }

    private ConfigResponse toResponse(PointConfig config) {
        return ConfigResponse.builder()
                .id(config.getId())
                .configKey(config.getConfigKey())
                .configValue(config.getConfigValue())
                .description(config.getDescription())
                .build();
    }
}

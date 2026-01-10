package com.point.repository;

import com.point.domain.PointConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PointConfigRepository extends JpaRepository<PointConfig, Long> {

    Optional<PointConfig> findByConfigKey(String configKey);
}

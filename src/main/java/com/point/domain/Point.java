package com.point.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "points", indexes = {
        @Index(name = "idx_point_member_status", columnList = "memberId, status"),
        @Index(name = "idx_point_expiration", columnList = "expirationDate")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Point {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long originalAmount;

    @Column(nullable = false)
    private Long remainingAmount;

    @Column(nullable = false)
    private boolean manual;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointStatus status;

    @Column(nullable = false)
    private LocalDateTime expirationDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Long earnTransactionId;

    @Builder
    public Point(Long memberId, Long originalAmount, boolean manual, LocalDateTime expirationDate, Long earnTransactionId) {
        this.memberId = memberId;
        this.originalAmount = originalAmount;
        this.remainingAmount = originalAmount;
        this.manual = manual;
        this.status = PointStatus.ACTIVE;
        this.expirationDate = expirationDate;
        this.earnTransactionId = earnTransactionId;
        this.createdAt = LocalDateTime.now();
    }

    public void use(Long amount) {
        if (this.remainingAmount < amount) {
            throw new IllegalArgumentException("Insufficient remaining amount");
        }
        this.remainingAmount -= amount;
    }

    public void restore(Long amount) {
        if (this.remainingAmount + amount > this.originalAmount) {
            throw new IllegalArgumentException("Cannot restore more than original amount");
        }
        this.remainingAmount += amount;
    }

    public void cancel() {
        if (this.remainingAmount < this.originalAmount) {
            throw new IllegalStateException("Cannot cancel point that has been partially used");
        }
        this.status = PointStatus.CANCELLED;
        this.remainingAmount = 0L;
    }

    public void expire() {
        this.status = PointStatus.EXPIRED;
    }

    public boolean isExpired() {
        return this.status == PointStatus.EXPIRED || LocalDateTime.now().isAfter(this.expirationDate);
    }

    public boolean isUsable() {
        return this.status == PointStatus.ACTIVE && !isExpired() && this.remainingAmount > 0;
    }

    public Long getUsedAmount() {
        return this.originalAmount - this.remainingAmount;
    }
}

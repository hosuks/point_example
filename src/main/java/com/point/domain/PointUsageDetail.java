package com.point.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "point_usage_details", indexes = {
        @Index(name = "idx_usage_detail_point", columnList = "pointId"),
        @Index(name = "idx_usage_detail_transaction", columnList = "transactionId")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointUsageDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transactionId", nullable = false)
    private PointTransaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pointId", nullable = false)
    private Point point;

    @Column(nullable = false)
    private Long usedAmount;

    @Column(nullable = false)
    private Long cancelledAmount = 0L;

    @Builder
    public PointUsageDetail(Point point, Long usedAmount) {
        this.point = point;
        this.usedAmount = usedAmount;
        this.cancelledAmount = 0L;
    }

    public void cancel(Long amount) {
        if (this.cancelledAmount + amount > this.usedAmount) {
            throw new IllegalArgumentException("Cannot cancel more than used amount");
        }
        this.cancelledAmount += amount;
    }

    public Long getRemainingCancellableAmount() {
        return this.usedAmount - this.cancelledAmount;
    }
}

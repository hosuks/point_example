package com.point.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "point_transactions", indexes = {
        @Index(name = "idx_transaction_member", columnList = "memberId"),
        @Index(name = "idx_transaction_point_key", columnList = "pointKey"),
        @Index(name = "idx_transaction_order_id", columnList = "orderId")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String pointKey;

    @Column(nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private Long amount;

    private String orderId;

    private Long relatedTransactionId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PointUsageDetail> usageDetails = new ArrayList<>();

    @Column(nullable = false)
    private Long cancelledAmount = 0L;

    @Builder
    public PointTransaction(Long memberId, TransactionType type, Long amount, String orderId, Long relatedTransactionId) {
        this.pointKey = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.memberId = memberId;
        this.type = type;
        this.amount = amount;
        this.orderId = orderId;
        this.relatedTransactionId = relatedTransactionId;
        this.createdAt = LocalDateTime.now();
        this.cancelledAmount = 0L;
    }

    public void addUsageDetail(PointUsageDetail detail) {
        this.usageDetails.add(detail);
        detail.setTransaction(this);
    }

    public void addCancelledAmount(Long amount) {
        if (this.cancelledAmount + amount > this.amount) {
            throw new IllegalArgumentException("Cannot cancel more than used amount");
        }
        this.cancelledAmount += amount;
    }

    public Long getRemainingCancellableAmount() {
        return this.amount - this.cancelledAmount;
    }
}

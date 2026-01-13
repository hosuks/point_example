package com.point.service;

import com.point.domain.*;
import com.point.dto.*;
import com.point.exception.PointErrorCode;
import com.point.exception.PointException;
import com.point.repository.PointRepository;
import com.point.repository.PointTransactionRepository;
import com.point.repository.PointUsageDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;
    private final PointTransactionRepository transactionRepository;
    private final PointUsageDetailRepository usageDetailRepository;
    private final PointConfigService configService;

    @Transactional
    public EarnResponse earn(EarnRequest request) {
        validateEarnAmount(request.getAmount());
        validateExpiryDays(request.getExpiryDays());
        validateMaxBalance(request.getMemberId(), request.getAmount());

        int expiryDays = request.getExpiryDays() != null
                ? request.getExpiryDays()
                : configService.getDefaultExpiryDays();

        LocalDateTime expirationDate = LocalDateTime.now().plusDays(expiryDays);

        PointTransaction transaction = PointTransaction.builder()
                .memberId(request.getMemberId())
                .type(TransactionType.EARN)
                .amount(request.getAmount())
                .build();

        transaction = transactionRepository.save(transaction);

        Point point = Point.builder()
                .memberId(request.getMemberId())
                .originalAmount(request.getAmount())
                .manual(request.isManual())
                .expirationDate(expirationDate)
                .earnTransactionId(transaction.getId())
                .build();

        pointRepository.save(point);

        log.info("Point earned: pointKey={}, memberId={}, amount={}, manual={}, expirationDate={}",
                transaction.getPointKey(), request.getMemberId(), request.getAmount(),
                request.isManual(), expirationDate);

        return EarnResponse.builder()
                .pointKey(transaction.getPointKey())
                .memberId(request.getMemberId())
                .amount(request.getAmount())
                .manual(request.isManual())
                .expirationDate(expirationDate)
                .balance(getBalance(request.getMemberId()))
                .build();
    }

    @Transactional
    public CancelEarnResponse cancelEarn(CancelEarnRequest request) {
        PointTransaction earnTransaction = transactionRepository.findByPointKey(request.getPointKey())
                .orElseThrow(() -> new PointException(PointErrorCode.TRANSACTION_NOT_FOUND));

        if (earnTransaction.getType() != TransactionType.EARN) {
            throw new PointException(PointErrorCode.INVALID_TRANSACTION_TYPE, "Only EARN transaction can be cancelled");
        }

        Point point = pointRepository.findByEarnTransactionId(earnTransaction.getId())
                .orElseThrow(() -> new PointException(PointErrorCode.POINT_NOT_FOUND));

        if (point.getUsedAmount() > 0) {
            throw new PointException(PointErrorCode.CANNOT_CANCEL_USED_POINT,
                    String.format("Used amount: %d", point.getUsedAmount()));
        }

        point.cancel();
        pointRepository.save(point);

        PointTransaction cancelTransaction = PointTransaction.builder()
                .memberId(earnTransaction.getMemberId())
                .type(TransactionType.EARN_CANCEL)
                .amount(earnTransaction.getAmount())
                .relatedTransactionId(earnTransaction.getId())
                .build();

        cancelTransaction = transactionRepository.save(cancelTransaction);

        log.info("Point earn cancelled: originalPointKey={}, cancelPointKey={}, amount={}",
                earnTransaction.getPointKey(), cancelTransaction.getPointKey(), earnTransaction.getAmount());

        return CancelEarnResponse.builder()
                .pointKey(cancelTransaction.getPointKey())
                .originalPointKey(earnTransaction.getPointKey())
                .memberId(earnTransaction.getMemberId())
                .cancelledAmount(earnTransaction.getAmount())
                .balance(getBalance(earnTransaction.getMemberId()))
                .build();
    }

    @Transactional
    public UseResponse use(UseRequest request) {
        if (request.getAmount() <= 0) {
            throw new PointException(PointErrorCode.INVALID_USE_AMOUNT);
        }

        if (request.getOrderId() == null || request.getOrderId().isBlank()) {
            throw new PointException(PointErrorCode.ORDER_ID_REQUIRED);
        }

        Long balance = getBalance(request.getMemberId());
        if (balance < request.getAmount()) {
            throw new PointException(PointErrorCode.INSUFFICIENT_BALANCE,
                    String.format("Balance: %d, Requested: %d", balance, request.getAmount()));
        }

        List<Point> usablePoints = pointRepository.findUsablePointsOrderByManualAndExpiration(
                request.getMemberId(), LocalDateTime.now());

        PointTransaction transaction = PointTransaction.builder()
                .memberId(request.getMemberId())
                .type(TransactionType.USE)
                .amount(request.getAmount())
                .orderId(request.getOrderId())
                .build();

        transaction = transactionRepository.save(transaction);

        Long remainingAmount = request.getAmount();
        List<PointUsageDetail> usageDetails = new ArrayList<>();

        for (Point point : usablePoints) {
            if (remainingAmount <= 0) break;

            Long useAmount = Math.min(point.getRemainingAmount(), remainingAmount);
            point.use(useAmount);
            pointRepository.save(point);

            PointUsageDetail detail = PointUsageDetail.builder()
                    .point(point)
                    .usedAmount(useAmount)
                    .build();

            transaction.addUsageDetail(detail);
            usageDetails.add(detail);

            remainingAmount -= useAmount;

            log.debug("Point used from: pointId={}, usedAmount={}, remainingInPoint={}",
                    point.getId(), useAmount, point.getRemainingAmount());
        }

        transactionRepository.save(transaction);

        log.info("Point used: pointKey={}, memberId={}, amount={}, orderId={}",
                transaction.getPointKey(), request.getMemberId(), request.getAmount(), request.getOrderId());

        return UseResponse.builder()
                .pointKey(transaction.getPointKey())
                .memberId(request.getMemberId())
                .usedAmount(request.getAmount())
                .orderId(request.getOrderId())
                .balance(getBalance(request.getMemberId()))
                .usageDetails(usageDetails.stream()
                        .map(d -> UseResponse.UsageDetailDto.builder()
                                .pointId(d.getPoint().getId())
                                .usedAmount(d.getUsedAmount())
                                .build())
                        .toList())
                .build();
    }

    @Transactional
    public CancelUseResponse cancelUse(CancelUseRequest request) {
        if (request.getAmount() <= 0) {
            throw new PointException(PointErrorCode.INVALID_CANCEL_AMOUNT);
        }

        PointTransaction useTransaction = transactionRepository.findByPointKeyWithUsageDetails(request.getPointKey())
                .orElseThrow(() -> new PointException(PointErrorCode.TRANSACTION_NOT_FOUND));

        if (useTransaction.getType() != TransactionType.USE) {
            throw new PointException(PointErrorCode.INVALID_TRANSACTION_TYPE, "Only USE transaction can be use-cancelled");
        }

        Long cancellableAmount = useTransaction.getRemainingCancellableAmount();
        if (request.getAmount() > cancellableAmount) {
            throw new PointException(PointErrorCode.EXCEED_CANCELLABLE_AMOUNT,
                    String.format("Cancellable: %d, Requested: %d", cancellableAmount, request.getAmount()));
        }

        PointTransaction cancelTransaction = PointTransaction.builder()
                .memberId(useTransaction.getMemberId())
                .type(TransactionType.USE_CANCEL)
                .amount(request.getAmount())
                .orderId(useTransaction.getOrderId())
                .relatedTransactionId(useTransaction.getId())
                .build();

        cancelTransaction = transactionRepository.save(cancelTransaction);

        List<PointUsageDetail> usageDetails = usageDetailRepository
                .findByTransactionIdWithPointOrderByExpirationAsc(useTransaction.getId());

        Long remainingCancelAmount = request.getAmount();
        List<CancelUseResponse.CancelDetailDto> cancelDetails = new ArrayList<>();
        List<CancelUseResponse.NewEarnDto> newEarns = new ArrayList<>();

        for (PointUsageDetail detail : usageDetails) {
            if (remainingCancelAmount <= 0) break;

            Long cancellableFromDetail = detail.getRemainingCancellableAmount();
            if (cancellableFromDetail <= 0) continue;

            Long cancelAmount = Math.min(cancellableFromDetail, remainingCancelAmount);
            detail.cancel(cancelAmount);

            Point point = detail.getPoint();
            boolean wasExpired = point.isExpired();

            if (wasExpired) {
                EarnResponse newEarn = earn(EarnRequest.builder()
                        .memberId(useTransaction.getMemberId())
                        .amount(cancelAmount)
                        .manual(point.isManual())
                        .expiryDays(configService.getDefaultExpiryDays())
                        .build());

                newEarns.add(CancelUseResponse.NewEarnDto.builder()
                        .pointKey(newEarn.getPointKey())
                        .amount(cancelAmount)
                        .reason("Original point expired - new point created")
                        .build());

                log.info("New point created for expired point: originalPointId={}, newPointKey={}, amount={}",
                        point.getId(), newEarn.getPointKey(), cancelAmount);
            } else {
                point.restore(cancelAmount);
                pointRepository.save(point);

                log.debug("Point restored: pointId={}, restoredAmount={}, newRemainingAmount={}",
                        point.getId(), cancelAmount, point.getRemainingAmount());
            }

            cancelDetails.add(CancelUseResponse.CancelDetailDto.builder()
                    .originalPointId(point.getId())
                    .cancelledAmount(cancelAmount)
                    .expired(wasExpired)
                    .build());

            remainingCancelAmount -= cancelAmount;
        }

        useTransaction.addCancelledAmount(request.getAmount());
        transactionRepository.save(useTransaction);
        usageDetailRepository.saveAll(usageDetails);

        log.info("Point use cancelled: originalPointKey={}, cancelPointKey={}, cancelledAmount={}",
                useTransaction.getPointKey(), cancelTransaction.getPointKey(), request.getAmount());

        return CancelUseResponse.builder()
                .pointKey(cancelTransaction.getPointKey())
                .originalPointKey(useTransaction.getPointKey())
                .memberId(useTransaction.getMemberId())
                .cancelledAmount(request.getAmount())
                .remainingCancellableAmount(useTransaction.getRemainingCancellableAmount())
                .balance(getBalance(useTransaction.getMemberId()))
                .cancelDetails(cancelDetails)
                .newEarns(newEarns)
                .build();
    }

    @Transactional(readOnly = true)
    public Long getBalance(Long memberId) {
        return pointRepository.sumRemainingAmountByMemberId(memberId, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public BalanceResponse getBalanceDetail(Long memberId) {
        Long balance = getBalance(memberId);
        List<Point> activePoints = pointRepository.findUsablePointsOrderByManualAndExpiration(
                memberId, LocalDateTime.now());

        List<BalanceResponse.PointDetailDto> pointDetails = activePoints.stream()
                .map(p -> BalanceResponse.PointDetailDto.builder()
                        .pointId(p.getId())
                        .originalAmount(p.getOriginalAmount())
                        .remainingAmount(p.getRemainingAmount())
                        .manual(p.isManual())
                        .expirationDate(p.getExpirationDate())
                        .build())
                .toList();

        return BalanceResponse.builder()
                .memberId(memberId)
                .balance(balance)
                .points(pointDetails)
                .build();
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionHistory(Long memberId) {
        return transactionRepository.findByMemberIdOrderByCreatedAtDesc(memberId)
                .stream()
                .map(t -> TransactionResponse.builder()
                        .pointKey(t.getPointKey())
                        .type(t.getType())
                        .amount(t.getAmount())
                        .orderId(t.getOrderId())
                        .cancelledAmount(t.getCancelledAmount())
                        .createdAt(t.getCreatedAt())
                        .build())
                .toList();
    }

    private void validateEarnAmount(Long amount) {
        Long minAmount = configService.getMinEarnAmount();
        Long maxAmount = configService.getMaxEarnAmount();

        if (amount < minAmount || amount > maxAmount) {
            throw new PointException(PointErrorCode.INVALID_EARN_AMOUNT,
                    String.format("Amount must be between %d and %d", minAmount, maxAmount));
        }
    }

    private void validateExpiryDays(Integer expiryDays) {
        if (expiryDays == null) return;

        Integer minDays = configService.getMinExpiryDays();
        Integer maxDays = configService.getMaxExpiryDays();

        if (expiryDays < minDays || expiryDays >= maxDays) {
            throw new PointException(PointErrorCode.INVALID_EXPIRY_DAYS,
                    String.format("Expiry days must be between %d and %d (exclusive)", minDays, maxDays));
        }
    }

    private void validateMaxBalance(Long memberId, Long earnAmount) {
        Long currentBalance = getBalance(memberId);
        Long maxBalance = configService.getMaxBalancePerMember();

        if (currentBalance + earnAmount > maxBalance) {
            throw new PointException(PointErrorCode.EXCEED_MAX_BALANCE,
                    String.format("Current: %d, Earn: %d, Max: %d", currentBalance, earnAmount, maxBalance));
        }
    }
}

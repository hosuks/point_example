package com.point.repository;

import com.point.domain.Point;
import com.point.domain.PointStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PointRepository extends JpaRepository<Point, Long> {

    @Query("SELECT COALESCE(SUM(p.remainingAmount), 0) FROM Point p WHERE p.memberId = :memberId AND p.status = 'ACTIVE' AND p.expirationDate > :now")
    Long sumRemainingAmountByMemberId(@Param("memberId") Long memberId, @Param("now") LocalDateTime now);

    @Query("SELECT p FROM Point p WHERE p.memberId = :memberId AND p.status = 'ACTIVE' AND p.expirationDate > :now AND p.remainingAmount > 0 ORDER BY p.manual DESC, p.expirationDate ASC")
    List<Point> findUsablePointsOrderByManualAndExpiration(@Param("memberId") Long memberId, @Param("now") LocalDateTime now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Point p WHERE p.id = :id")
    Optional<Point> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT p FROM Point p WHERE p.earnTransactionId = :transactionId")
    Optional<Point> findByEarnTransactionId(@Param("transactionId") Long transactionId);

    List<Point> findByMemberIdAndStatus(Long memberId, PointStatus status);

    @Query("SELECT p FROM Point p WHERE p.status = 'ACTIVE' AND p.expirationDate <= :now")
    List<Point> findExpiredPoints(@Param("now") LocalDateTime now);
}

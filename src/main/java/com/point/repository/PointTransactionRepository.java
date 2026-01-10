package com.point.repository;

import com.point.domain.PointTransaction;
import com.point.domain.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    Optional<PointTransaction> findByPointKey(String pointKey);

    List<PointTransaction> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    List<PointTransaction> findByOrderId(String orderId);

    @Query("SELECT t FROM PointTransaction t WHERE t.memberId = :memberId AND t.type = :type ORDER BY t.createdAt DESC")
    List<PointTransaction> findByMemberIdAndType(@Param("memberId") Long memberId, @Param("type") TransactionType type);

    @Query("SELECT t FROM PointTransaction t LEFT JOIN FETCH t.usageDetails WHERE t.pointKey = :pointKey")
    Optional<PointTransaction> findByPointKeyWithUsageDetails(@Param("pointKey") String pointKey);
}

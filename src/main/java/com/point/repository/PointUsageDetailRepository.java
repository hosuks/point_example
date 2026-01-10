package com.point.repository;

import com.point.domain.PointUsageDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointUsageDetailRepository extends JpaRepository<PointUsageDetail, Long> {

    @Query("SELECT ud FROM PointUsageDetail ud WHERE ud.transaction.id = :transactionId")
    List<PointUsageDetail> findByTransactionId(@Param("transactionId") Long transactionId);

    @Query("SELECT ud FROM PointUsageDetail ud JOIN FETCH ud.point WHERE ud.transaction.id = :transactionId ORDER BY ud.point.expirationDate ASC")
    List<PointUsageDetail> findByTransactionIdWithPointOrderByExpirationAsc(@Param("transactionId") Long transactionId);

    @Query("SELECT ud FROM PointUsageDetail ud WHERE ud.point.id = :pointId")
    List<PointUsageDetail> findByPointId(@Param("pointId") Long pointId);
}

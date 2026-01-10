package com.point.service;

import com.point.domain.Point;
import com.point.domain.PointStatus;
import com.point.dto.*;
import com.point.exception.PointException;
import com.point.repository.PointRepository;
import com.point.repository.PointTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PointServiceTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private PointTransactionRepository transactionRepository;

    private static final Long MEMBER_ID = 1L;

    @Nested
    @DisplayName("적립 테스트")
    class EarnTest {

        @Test
        @DisplayName("정상적으로 포인트를 적립한다")
        void earnSuccess() {
            EarnRequest request = EarnRequest.builder()
                    .memberId(MEMBER_ID)
                    .amount(1000L)
                    .manual(false)
                    .build();

            EarnResponse response = pointService.earn(request);

            assertThat(response.getPointKey()).isNotBlank();
            assertThat(response.getAmount()).isEqualTo(1000L);
            assertThat(response.getBalance()).isEqualTo(1000L);
            assertThat(response.isManual()).isFalse();
        }

        @Test
        @DisplayName("수기 지급 포인트를 적립한다")
        void earnManualSuccess() {
            EarnRequest request = EarnRequest.builder()
                    .memberId(MEMBER_ID)
                    .amount(500L)
                    .manual(true)
                    .build();

            EarnResponse response = pointService.earn(request);

            assertThat(response.isManual()).isTrue();
        }

        @Test
        @DisplayName("만료일을 지정하여 적립한다")
        void earnWithCustomExpiry() {
            EarnRequest request = EarnRequest.builder()
                    .memberId(MEMBER_ID)
                    .amount(1000L)
                    .expiryDays(30)
                    .build();

            EarnResponse response = pointService.earn(request);

            assertThat(response.getExpirationDate())
                    .isAfter(LocalDateTime.now().plusDays(29))
                    .isBefore(LocalDateTime.now().plusDays(31));
        }

        @Test
        @DisplayName("최소 적립금액 미만이면 예외가 발생한다")
        void earnBelowMinAmount() {
            EarnRequest request = EarnRequest.builder()
                    .memberId(MEMBER_ID)
                    .amount(0L)
                    .build();

            assertThatThrownBy(() -> pointService.earn(request))
                    .isInstanceOf(PointException.class);
        }

        @Test
        @DisplayName("최대 적립금액 초과시 예외가 발생한다")
        void earnExceedMaxAmount() {
            EarnRequest request = EarnRequest.builder()
                    .memberId(MEMBER_ID)
                    .amount(100001L)
                    .build();

            assertThatThrownBy(() -> pointService.earn(request))
                    .isInstanceOf(PointException.class);
        }
    }

    @Nested
    @DisplayName("적립 취소 테스트")
    class CancelEarnTest {

        @Test
        @DisplayName("정상적으로 적립을 취소한다")
        void cancelEarnSuccess() {
            EarnResponse earnResponse = pointService.earn(EarnRequest.builder()
                    .memberId(MEMBER_ID)
                    .amount(1000L)
                    .build());

            CancelEarnResponse cancelResponse = pointService.cancelEarn(
                    CancelEarnRequest.builder()
                            .pointKey(earnResponse.getPointKey())
                            .build());

            assertThat(cancelResponse.getCancelledAmount()).isEqualTo(1000L);
            assertThat(cancelResponse.getBalance()).isEqualTo(0L);
        }

        @Test
        @DisplayName("일부라도 사용된 포인트는 적립 취소할 수 없다")
        void cannotCancelUsedPoint() {
            EarnResponse earnResponse = pointService.earn(EarnRequest.builder()
                    .memberId(MEMBER_ID)
                    .amount(1000L)
                    .build());

            pointService.use(UseRequest.builder()
                    .memberId(MEMBER_ID)
                    .amount(500L)
                    .orderId("ORDER001")
                    .build());

            assertThatThrownBy(() -> pointService.cancelEarn(
                    CancelEarnRequest.builder()
                            .pointKey(earnResponse.getPointKey())
                            .build()))
                    .isInstanceOf(PointException.class);
        }
    }

    @Nested
    @DisplayName("사용 테스트")
    class UseTest {

        @Test
        @DisplayName("정상적으로 포인트를 사용한다")
        void useSuccess() {
            pointService.earn(EarnRequest.builder()
                    .memberId(MEMBER_ID)
                    .amount(1000L)
                    .build());

            UseResponse response = pointService.use(UseRequest.builder()
                    .memberId(MEMBER_ID)
                    .amount(500L)
                    .orderId("ORDER001")
                    .build());

            assertThat(response.getUsedAmount()).isEqualTo(500L);
            assertThat(response.getOrderId()).isEqualTo("ORDER001");
            assertThat(response.getBalance()).isEqualTo(500L);
        }

        @Test
        @DisplayName("수기 지급 포인트가 우선 사용된다")
        void manualPointUsedFirst() {
            // 일반 포인트 적립
            pointService.earn(EarnRequest.builder()
                    .memberId(MEMBER_ID)
                    .amount(1000L)
                    .manual(false)
                    .build());

            // 수기 지급 포인트 적립
            pointService.earn(EarnRequest.builder()
                    .memberId(MEMBER_ID)
                    .amount(500L)
                    .manual(true)
                    .build());

            UseResponse response = pointService.use(UseRequest.builder()
                    .memberId(MEMBER_ID)
                    .amount(700L)
                    .orderId("ORDER001")
                    .build());

            assertThat(response.getUsageDetails()).hasSize(2);
            // 수기 지급 500원 먼저, 그 다음 일반 200원
            assertThat(response.getUsageDetails().get(0).getUsedAmount()).isEqualTo(500L);
            assertThat(response.getUsageDetails().get(1).getUsedAmount()).isEqualTo(200L);
        }

        @Test
        @DisplayName("잔액 부족시 예외가 발생한다")
        void insufficientBalance() {
            pointService.earn(EarnRequest.builder()
                    .memberId(MEMBER_ID)
                    .amount(1000L)
                    .build());

            assertThatThrownBy(() -> pointService.use(UseRequest.builder()
                    .memberId(MEMBER_ID)
                    .amount(2000L)
                    .orderId("ORDER001")
                    .build()))
                    .isInstanceOf(PointException.class);
        }

        @Test
        @DisplayName("주문번호 없이 사용하면 예외가 발생한다")
        void useWithoutOrderId() {
            pointService.earn(EarnRequest.builder()
                    .memberId(MEMBER_ID)
                    .amount(1000L)
                    .build());

            assertThatThrownBy(() -> pointService.use(UseRequest.builder()
                    .memberId(MEMBER_ID)
                    .amount(500L)
                    .orderId("")
                    .build()))
                    .isInstanceOf(PointException.class);
        }
    }

    @Nested
    @DisplayName("사용 취소 테스트")
    class CancelUseTest {

        @Test
        @DisplayName("전체 사용 취소를 한다")
        void cancelUseFullAmount() {
            pointService.earn(EarnRequest.builder()
                    .memberId(MEMBER_ID)
                    .amount(1000L)
                    .build());

            UseResponse useResponse = pointService.use(UseRequest.builder()
                    .memberId(MEMBER_ID)
                    .amount(500L)
                    .orderId("ORDER001")
                    .build());

            CancelUseResponse cancelResponse = pointService.cancelUse(CancelUseRequest.builder()
                    .pointKey(useResponse.getPointKey())
                    .amount(500L)
                    .build());

            assertThat(cancelResponse.getCancelledAmount()).isEqualTo(500L);
            assertThat(cancelResponse.getBalance()).isEqualTo(1000L);
            assertThat(cancelResponse.getRemainingCancellableAmount()).isEqualTo(0L);
        }

        @Test
        @DisplayName("부분 사용 취소를 한다")
        void cancelUsePartialAmount() {
            pointService.earn(EarnRequest.builder()
                    .memberId(MEMBER_ID)
                    .amount(1000L)
                    .build());

            UseResponse useResponse = pointService.use(UseRequest.builder()
                    .memberId(MEMBER_ID)
                    .amount(500L)
                    .orderId("ORDER001")
                    .build());

            CancelUseResponse cancelResponse = pointService.cancelUse(CancelUseRequest.builder()
                    .pointKey(useResponse.getPointKey())
                    .amount(300L)
                    .build());

            assertThat(cancelResponse.getCancelledAmount()).isEqualTo(300L);
            assertThat(cancelResponse.getBalance()).isEqualTo(800L);
            assertThat(cancelResponse.getRemainingCancellableAmount()).isEqualTo(200L);
        }

        @Test
        @DisplayName("취소 가능 금액을 초과하면 예외가 발생한다")
        void exceedCancellableAmount() {
            pointService.earn(EarnRequest.builder()
                    .memberId(MEMBER_ID)
                    .amount(1000L)
                    .build());

            UseResponse useResponse = pointService.use(UseRequest.builder()
                    .memberId(MEMBER_ID)
                    .amount(500L)
                    .orderId("ORDER001")
                    .build());

            assertThatThrownBy(() -> pointService.cancelUse(CancelUseRequest.builder()
                    .pointKey(useResponse.getPointKey())
                    .amount(600L)
                    .build()))
                    .isInstanceOf(PointException.class);
        }
    }

    @Nested
    @DisplayName("예시 시나리오 테스트")
    class ExampleScenarioTest {

        @Test
        @DisplayName("예시 시나리오를 검증한다")
        @Transactional
        void exampleScenario() {
            // 1. 1000원 적립 (총 잔액 0 -> 1000원)
            EarnResponse earnA = pointService.earn(EarnRequest.builder()
                    .memberId(MEMBER_ID)
                    .amount(1000L)
                    .expiryDays(1) // 테스트를 위해 1일로 설정
                    .build());
            assertThat(earnA.getBalance()).isEqualTo(1000L);

            // 2. 500원 적립 (총 잔액 1000 -> 1500원)
            EarnResponse earnB = pointService.earn(EarnRequest.builder()
                    .memberId(MEMBER_ID)
                    .amount(500L)
                    .expiryDays(365)
                    .build());
            assertThat(earnB.getBalance()).isEqualTo(1500L);

            // 3. 주문번호 A1234에서 1200원 사용 (총 잔액 1500 -> 300원)
            UseResponse useC = pointService.use(UseRequest.builder()
                    .memberId(MEMBER_ID)
                    .amount(1200L)
                    .orderId("A1234")
                    .build());
            assertThat(useC.getBalance()).isEqualTo(300L);
            assertThat(useC.getUsageDetails()).hasSize(2);

            // 4. A의 적립이 만료되었다 (시뮬레이션)
            Point pointA = pointRepository.findByEarnTransactionId(
                    transactionRepository.findByPointKey(earnA.getPointKey()).get().getId()
            ).get();
            pointA.expire();
            pointRepository.save(pointA);

            // 5. C의 사용금액 1200원 중 1100원을 부분 사용취소 (총 잔액 300 -> 1400원)
            CancelUseResponse cancelD = pointService.cancelUse(CancelUseRequest.builder()
                    .pointKey(useC.getPointKey())
                    .amount(1100L)
                    .build());

            assertThat(cancelD.getCancelledAmount()).isEqualTo(1100L);
            // A는 만료되어 새로운 포인트 E가 생성되어야 함
            assertThat(cancelD.getNewEarns()).isNotEmpty();
            assertThat(cancelD.getNewEarns().stream()
                    .mapToLong(CancelUseResponse.NewEarnDto::getAmount)
                    .sum()).isEqualTo(1000L);

            // B의 잔액은 300 -> 400원
            Point pointB = pointRepository.findByEarnTransactionId(
                    transactionRepository.findByPointKey(earnB.getPointKey()).get().getId()
            ).get();
            assertThat(pointB.getRemainingAmount()).isEqualTo(400L);

            // C는 이제 100원만 취소 가능
            assertThat(cancelD.getRemainingCancellableAmount()).isEqualTo(100L);

            // 최종 잔액 확인 (새로 적립된 1000원 + B의 400원 = 1400원)
            assertThat(cancelD.getBalance()).isEqualTo(1400L);
        }
    }

    @Nested
    @DisplayName("잔액 조회 테스트")
    class BalanceTest {

        @Test
        @DisplayName("잔액 상세 정보를 조회한다")
        void getBalanceDetail() {
            pointService.earn(EarnRequest.builder()
                    .memberId(MEMBER_ID)
                    .amount(1000L)
                    .manual(false)
                    .build());

            pointService.earn(EarnRequest.builder()
                    .memberId(MEMBER_ID)
                    .amount(500L)
                    .manual(true)
                    .build());

            BalanceResponse response = pointService.getBalanceDetail(MEMBER_ID);

            assertThat(response.getBalance()).isEqualTo(1500L);
            assertThat(response.getPoints()).hasSize(2);
        }
    }
}

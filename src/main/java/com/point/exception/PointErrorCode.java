package com.point.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PointErrorCode {

    // Earn errors
    INVALID_EARN_AMOUNT(HttpStatus.BAD_REQUEST, "POINT_001", "적립 금액이 유효하지 않습니다"),
    EXCEED_MAX_BALANCE(HttpStatus.BAD_REQUEST, "POINT_002", "최대 보유 가능 포인트를 초과합니다"),
    INVALID_EXPIRY_DAYS(HttpStatus.BAD_REQUEST, "POINT_003", "만료일이 유효하지 않습니다"),

    // Cancel earn errors
    TRANSACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "POINT_004", "트랜잭션을 찾을 수 없습니다"),
    POINT_NOT_FOUND(HttpStatus.NOT_FOUND, "POINT_005", "포인트를 찾을 수 없습니다"),
    CANNOT_CANCEL_USED_POINT(HttpStatus.BAD_REQUEST, "POINT_006", "사용된 포인트는 적립 취소할 수 없습니다"),
    INVALID_TRANSACTION_TYPE(HttpStatus.BAD_REQUEST, "POINT_007", "유효하지 않은 트랜잭션 타입입니다"),

    // Use errors
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "POINT_008", "잔액이 부족합니다"),
    INVALID_USE_AMOUNT(HttpStatus.BAD_REQUEST, "POINT_009", "사용 금액이 유효하지 않습니다"),
    ORDER_ID_REQUIRED(HttpStatus.BAD_REQUEST, "POINT_010", "주문번호가 필요합니다"),

    // Cancel use errors
    EXCEED_CANCELLABLE_AMOUNT(HttpStatus.BAD_REQUEST, "POINT_011", "취소 가능 금액을 초과합니다"),
    INVALID_CANCEL_AMOUNT(HttpStatus.BAD_REQUEST, "POINT_012", "취소 금액이 유효하지 않습니다"),

    // Common errors
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "POINT_013", "회원을 찾을 수 없습니다"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "POINT_999", "내부 서버 오류가 발생했습니다");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

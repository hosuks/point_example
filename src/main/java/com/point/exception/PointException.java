package com.point.exception;

import lombok.Getter;

@Getter
public class PointException extends RuntimeException {

    private final PointErrorCode errorCode;

    public PointException(PointErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public PointException(PointErrorCode errorCode, String additionalMessage) {
        super(errorCode.getMessage() + ": " + additionalMessage);
        this.errorCode = errorCode;
    }
}

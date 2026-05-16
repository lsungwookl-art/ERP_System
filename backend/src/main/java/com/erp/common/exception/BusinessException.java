package com.erp.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus status;

    public BusinessException(String message, String errorCode, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public static BusinessException notFound(String resource) {
        return new BusinessException(resource + "을(를) 찾을 수 없습니다.", "NOT_FOUND", HttpStatus.NOT_FOUND);
    }

    public static BusinessException badRequest(String message) {
        return new BusinessException(message, "BAD_REQUEST", HttpStatus.BAD_REQUEST);
    }

    public static BusinessException conflict(String message) {
        return new BusinessException(message, "CONFLICT", HttpStatus.CONFLICT);
    }

    public static BusinessException forbidden(String message) {
        return new BusinessException(message, "FORBIDDEN", HttpStatus.FORBIDDEN);
    }
}

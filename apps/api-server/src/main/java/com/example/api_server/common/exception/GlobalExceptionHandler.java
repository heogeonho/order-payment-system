package com.example.api_server.common.exception;

import com.example.api_server.common.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 전역 예외 핸들러
 * 모든 컨트롤러에서 발생하는 예외를 일관되게 처리
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 404 NOT FOUND 예외 처리
     */
    @ExceptionHandler({
            ProductNotFoundException.class,
            OrderNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFoundException(BusinessException ex) {
        log.warn("Not Found Exception: {}", ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(ex.getErrorCode(), ex.getMessage(), ex.getDetail());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * 409 CONFLICT 예외 처리
     */
    @ExceptionHandler({
            OrderNotPayableException.class,
            PaymentAlreadyApprovedException.class
    })
    public ResponseEntity<ErrorResponse> handleConflictException(BusinessException ex) {
        log.warn("Conflict Exception: {}", ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(ex.getErrorCode(), ex.getMessage(), ex.getDetail());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * 400 BAD REQUEST 예외 처리 (비즈니스 로직 위반)
     */
    @ExceptionHandler({
            ProductNotAvailableException.class,
            OutOfStockException.class,
            QuantityInvalidException.class,
            AmountMismatchException.class,
            PgApprovalFailedException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequestException(BusinessException ex) {
        log.warn("Bad Request Exception: {}", ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(ex.getErrorCode(), ex.getMessage(), ex.getDetail());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Validation 예외 처리 (Bean Validation)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation Exception: {}", ex.getMessage());

        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        String errorDetail = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorResponse errorResponse = ErrorResponse.of("VALIDATION_FAILED", errorMessage, errorDetail);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 처리되지 않은 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected Exception: ", ex);
        ErrorResponse errorResponse = ErrorResponse.of(
                "INTERNAL_SERVER_ERROR",
                "서버 내부 오류가 발생했습니다.",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

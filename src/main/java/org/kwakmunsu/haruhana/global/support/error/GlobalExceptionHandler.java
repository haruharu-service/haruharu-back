package org.kwakmunsu.haruhana.global.support.error;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kwakmunsu.haruhana.global.support.notification.ErrorNotificationSender;
import org.kwakmunsu.haruhana.global.support.response.ApiResponse;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ErrorNotificationSender errorNotificationSender;

    @ExceptionHandler(HaruHanaException.class)
    public ResponseEntity<ApiResponse<?>> handleCustomException(HaruHanaException e) {
        ErrorType errorType = e.getErrorType();
        Object data = e.getData();

        String logMessage = String.format("[%s] %s (Data: %s)",
                errorType.name(),
                e.getMessage(),
                data != null ? data.toString() : "null"
        );

        switch (errorType.getLogLevel()) {
            case LogLevel.ERROR -> {
                log.error(logMessage, e);
                errorNotificationSender.sendErrorNotification(logMessage, e);
            }
            case LogLevel.WARN ->  log.warn(logMessage);
            default ->             log.info(logMessage);
        }

        return ResponseEntity
                .status(errorType.getStatus())
                .body(ApiResponse.error(errorType, data));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        log.error("[Exception]: {}", e.getMessage(), e);
        errorNotificationSender.sendErrorNotification("[Exception]: " + e.getMessage(), e);

        ErrorType errorType = ErrorType.DEFAULT_ERROR;

        return ResponseEntity
                .status(errorType.getStatus())
                .body(ApiResponse.error(errorType, errorType.getStatus()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleConstraintViolationException(ConstraintViolationException e) {
        ErrorType errorType = ErrorType.BAD_REQUEST;

        Map<String, String> validationData = e.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (existing, replacement) -> existing + ", " + replacement
                ));

        log.warn("[ConstraintViolationException] @RequestParam 유효성 검사 실패. (ValidationData={})", validationData, e);

        return ResponseEntity
                .status(errorType.getStatus())
                .body(ApiResponse.error(errorType, validationData));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        ErrorType errorType = ErrorType.BAD_REQUEST;

        Map<String, String> validationData = e.getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "유효성 검사 실패",
                        (existing, replacement) -> existing + ", " + replacement
                ));

        log.warn("[MethodArgumentNotValidException] @Valid 실패. (ValidationData={})", validationData, e);

        return ResponseEntity
                .status(errorType.getStatus())
                .body(ApiResponse.error(errorType, validationData));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentMismatchException(MethodArgumentTypeMismatchException e) {
        ErrorType errorType = ErrorType.BAD_REQUEST;
        String paramName = e.getParameter().getParameterName();
        String paramType = e.getParameter().getParameterType().getSimpleName();
        String detailMessage = e.getMessage();
        String message = "[" + paramName + "] 파라미터는 " + paramType + " 타입이어야 합니다. 상세: " + detailMessage;

        log.warn("[MethodArgumentTypeMismatchException]: {}", message);

        return ResponseEntity
                .status(errorType.getStatus())
                .body(ApiResponse.error(errorType, message));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<?>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e) {
        ErrorType errorType = ErrorType.BAD_REQUEST;
        String paramName = e.getParameterName();
        String paramType = e.getParameterType();
        String message = paramType + " 타입의" + " [ " + paramName + " ] " + "파라미터가 누락되었습니다.";

        log.warn("[MissingServletRequestParameterException]: {}", message);

        return ResponseEntity
                .status(errorType.getStatus())
                .body(ApiResponse.error(errorType, message));
    }

}
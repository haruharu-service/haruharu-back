package org.kwakmunsu.haruhana.global.support.error;

import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kwakmunsu.haruhana.global.support.notification.ErrorNotificationSender;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.logging.LogLevel;

/**
 * 비동기 작업 중 발생한 예외를 처리하는 핸들러
 *
 * <p>@Async 메서드에서 발생한 예외를 잡아서 로깅합니다.
 * AsyncConfig 에서 이 핸들러를 등록하여 사용합니다.
 */
@Slf4j
@RequiredArgsConstructor
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    private final ErrorNotificationSender errorNotificationSender;

    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... params) {
        if (throwable instanceof HaruHanaException e) {
            String logMessage = String.format("비동기 작업 중 HaruHanaException 발생 - Method: %s, ErrorType: %s, Message: %s, Data: %s",
                    method.getName(),
                    e.getErrorType().name(),
                    e.getMessage(),
                    e.getData() != null ? e.getData().toString() : "null"
            );

            switch (e.getErrorType().getLogLevel()) {
                case LogLevel.ERROR -> {
                    log.error(logMessage, e);
                    errorNotificationSender.sendErrorNotification(logMessage, e);
                }
                case LogLevel.WARN ->  log.warn(logMessage, e);
                default ->             log.info(logMessage, e);
            }
        } else {
            String logMessage = String.format("비동기 작업 중 Exception 발생 - Method: %s, Error: %s",
                    method.getName(),
                    throwable.getMessage()
            );
            log.error("비동기 작업 중 Exception 발생 - Method: {}, Args: {}, Error: {}",
                    method.getName(),
                    params,
                    throwable.getMessage(),
                    throwable
            );
            errorNotificationSender.sendErrorNotification(logMessage, throwable);
        }
    }

}
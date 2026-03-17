package org.kwakmunsu.haruhana.domain.scheduler;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kwakmunsu.haruhana.domain.member.service.MemberDeviceManager;
import org.kwakmunsu.haruhana.global.support.notification.ErrorNotificationSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DeviceTokenScheduler {

    private static final int TOKEN_EXPIRATION_DAYS = 30;
    private static final int BATCH_SIZE = 500;
    private static final int MAX_ITERATIONS = 1000;

    private final MemberDeviceManager memberDeviceManager;
    private final ErrorNotificationSender errorNotificationSender;

    /**
     * 매일 03:00에 실행되어 30일 이상 동기화되지 않은 디바이스 토큰을 배치 단위로 삭제하는 스케줄러 <br>
     */
    @Scheduled(cron = "0 0 3 * * *") // 매일 03:00에 실행
    public void clearExpiredDeviceToken() {
        log.info("[DeviceTokenScheduler] 디바이스 토큰 정리 스케줄러 시작");

        int totalDeletedCnt = 0;
        int iterations = 0;
        LocalDateTime cutoffDateTime = LocalDateTime.now().minusDays(TOKEN_EXPIRATION_DAYS);

        try {
            while (iterations < MAX_ITERATIONS) {
                int deletedCnt = memberDeviceManager.deleteExpiredTokensBatch(cutoffDateTime, BATCH_SIZE);

                if (deletedCnt == 0) {
                    break;
                }

                totalDeletedCnt += deletedCnt;
                iterations++;
            }
            log.info("[DeviceTokenScheduler] 디바이스 토큰 정리 완료 (총 {}개 삭제)", totalDeletedCnt);
        } catch (Exception e) {
            log.error("[DeviceTokenScheduler] 디바이스 토큰 정리 중 오류 발생: {}", e.getMessage(), e);
            errorNotificationSender.sendErrorNotification("[DeviceTokenScheduler] 디바이스 토큰 정리 중 오류 발생: " + e.getMessage(), e);
        }
    }

}
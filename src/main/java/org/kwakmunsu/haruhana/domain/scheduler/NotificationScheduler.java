package org.kwakmunsu.haruhana.domain.scheduler;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kwakmunsu.haruhana.domain.dailyproblem.service.DailyProblemReader;
import org.kwakmunsu.haruhana.domain.member.service.MemberDeviceReader;
import org.kwakmunsu.haruhana.domain.notification.enums.NotificationMessage;
import org.kwakmunsu.haruhana.domain.notification.enums.NotificationType;
import org.kwakmunsu.haruhana.global.support.notification.ErrorNotificationSender;
import org.kwakmunsu.haruhana.global.support.notification.NotificationSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationScheduler {

    private final DailyProblemReader dailyProblemReader;
    private final MemberDeviceReader memberDeviceReader;
    private final NotificationSender notificationSender;
    private final ErrorNotificationSender errorNotificationSender;

    /**
     * 매일 21시, 오늘 문제를 풀지 않은 회원들에게 푸시 알림을 전송합니다.
     */
    // NOTE: 추후 성능 이슈 발생 시, bulk send 또는 Async 처리 고려
    @Scheduled(cron = "0 0 21 * * ?")
    public void notifyUnsolvedProblemMembers() {
        log.info("[NotificationScheduler] 일일 문제 미풀이 회원 알림 전송 시작");

        try {
            LocalDate today = LocalDate.now();
            List<String> deviceTokens = getUnsolvedMemberDeviceTokens(today);

            sendBulkNotifications(deviceTokens);
        } catch (Exception e) {
            log.error("[NotificationScheduler] 알림 전송 스케줄러 실행 중 예기치 않은 오류 발생", e);
            errorNotificationSender.sendErrorNotification("[NotificationScheduler] 알림 전송 스케줄러 실행 중 예기치 않은 오류 발생: " + e.getMessage(), e);
        }
    }

    private List<String> getUnsolvedMemberDeviceTokens(LocalDate date) {
        List<Long> unsolvedMemberIds = dailyProblemReader.findUnsolvedMember(date);

        if (unsolvedMemberIds.isEmpty()) {
            return List.of();
        }

        return memberDeviceReader.findDeviceTokensByMemberIds(unsolvedMemberIds);
    }

    private void sendBulkNotifications(List<String> deviceTokens) {
        deviceTokens.forEach(token -> notificationSender.sendNotification(
                token,
                NotificationMessage.UNSOLVED_PROBLEM_REMINDER.getTitle(),
                NotificationMessage.UNSOLVED_PROBLEM_REMINDER.getMessage(),
                NotificationType.UNSOLVED_PROBLEM_REMINDER
        ));
    }

}
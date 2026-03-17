package org.kwakmunsu.haruhana.domain.scheduler;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kwakmunsu.haruhana.domain.problem.service.ProblemGenerator;
import org.kwakmunsu.haruhana.global.support.notification.ErrorNotificationSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProblemScheduler {

    private final ProblemGenerator problemGenerator;
    private final ErrorNotificationSender errorNotificationSender;

    /**
     * 매일 23:55분에 익일자 문제를 생성하는 스케줄러
     * cron: 초 분 시 일 월 요일
     */
    @Scheduled(cron = "0 55 23 * * *")
    public void generateDailyProblems() {
        log.info("[ProblemScheduler] 일일 문제 생성 스케줄러 시작");
        try {
            LocalDate targetDate = LocalDate.now().plusDays(1);
            problemGenerator.generateProblem(targetDate);
            log.info("[ProblemScheduler] {} 문제 생성 완료", targetDate);
        } catch (Exception e) {
            log.error("[ProblemScheduler] 문제 생성 실패: {}", e.getMessage(), e);
            errorNotificationSender.sendErrorNotification("[ProblemScheduler] 문제 생성 실패: " + e.getMessage(), e);
        }
    }

}
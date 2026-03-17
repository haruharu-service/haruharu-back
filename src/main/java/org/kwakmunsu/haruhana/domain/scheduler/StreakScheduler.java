package org.kwakmunsu.haruhana.domain.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kwakmunsu.haruhana.domain.member.entity.Member;
import org.kwakmunsu.haruhana.domain.member.service.MemberReader;
import org.kwakmunsu.haruhana.domain.streak.service.StreakManager;
import org.kwakmunsu.haruhana.global.support.notification.ErrorNotificationSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class StreakScheduler {

    private final MemberReader memberReader;
    private final StreakManager streakManager;
    private final ErrorNotificationSender errorNotificationSender;

    /**
     * 매일 00: 00분에 전날 문제를 제출하지 않은 회원들의 스트릭을 초기화하는 스케줄러 <br>
     * cron: 초 분 시 일 월 요일
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void initStreakWhoDidNotSubmitYesterday() {
        log.info("[StreakScheduler] 전날 미제출 회원 스트릭 초기화 스케줄러 시작");

        LocalDateTime startOfYesterday = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

        // NOTE: 추후 id 값만 조회하도록 해서 성능 최적화 가능
        List<Member> members = memberReader.findMembersWithoutSubmissionBetween(startOfYesterday, startOfToday);

        // NOTE: 대량의 회원이 있을 경우 성능 이슈가 발생할 수 있으므로, 배치 처리 또는 청크 단위로 나누어 처리하는 방법 고려 필요
        for (Member member : members) {
            try {
                streakManager.initStreakForMember(member);
            } catch (Exception e) {
                log.warn("[StreakScheduler] 회원 스트릭 초기화 실패 - memberId: {}, error: {}", member.getId(), e.getMessage());
                errorNotificationSender.sendErrorNotification("[StreakScheduler] 회원 스트릭 초기화 실패 - memberId: " + member.getId(), e);
            }
        }

        log.info("[StreakScheduler] 전날 미제출 회원 스트릭 초기화 스케줄러 완료 - 초기화 회원 수: {}", members.size());
    }

}
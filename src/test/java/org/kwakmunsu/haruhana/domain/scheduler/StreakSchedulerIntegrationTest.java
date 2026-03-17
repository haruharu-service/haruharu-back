package org.kwakmunsu.haruhana.domain.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kwakmunsu.haruhana.IntegrationTestSupport;
import org.kwakmunsu.haruhana.domain.category.CategoryFactory;
import org.kwakmunsu.haruhana.domain.category.entity.CategoryTopic;
import org.kwakmunsu.haruhana.domain.category.repository.CategoryTopicJpaRepository;
import org.kwakmunsu.haruhana.domain.dailyproblem.entity.DailyProblem;
import org.kwakmunsu.haruhana.domain.dailyproblem.repository.DailyProblemJpaRepository;
import org.kwakmunsu.haruhana.domain.member.MemberFixture;
import org.kwakmunsu.haruhana.domain.member.entity.Member;
import org.kwakmunsu.haruhana.domain.member.enums.Role;
import org.kwakmunsu.haruhana.domain.member.repository.MemberJpaRepository;
import org.kwakmunsu.haruhana.domain.problem.entity.Problem;
import org.kwakmunsu.haruhana.domain.problem.enums.ProblemDifficulty;
import org.kwakmunsu.haruhana.domain.problem.repository.ProblemJpaRepository;
import org.kwakmunsu.haruhana.domain.streak.entity.Streak;
import org.kwakmunsu.haruhana.domain.streak.repository.StreakJpaRepository;
import org.kwakmunsu.haruhana.domain.submission.entity.Submission;
import org.kwakmunsu.haruhana.domain.submission.repository.SubmissionJpaRepository;
import org.kwakmunsu.haruhana.global.entity.EntityStatus;
import org.springframework.transaction.annotation.Transactional;

/**
 * StreakScheduler 통합 테스트
 * - 실제 DB를 사용하여 스케줄러 동작 검증
 * - 전날 미제출 회원의 스트릭 초기화 확인
 */
@Transactional
@RequiredArgsConstructor
class StreakSchedulerIntegrationTest extends IntegrationTestSupport {

    final CategoryFactory categoryFactory;
    final StreakScheduler streakScheduler;
    final MemberJpaRepository memberJpaRepository;
    final StreakJpaRepository streakJpaRepository;
    final CategoryTopicJpaRepository categoryTopicJpaRepository;
    final ProblemJpaRepository problemJpaRepository;
    final DailyProblemJpaRepository dailyProblemJpaRepository;
    final SubmissionJpaRepository submissionJpaRepository;

    private CategoryTopic categoryTopic;

    @BeforeEach
    void setUp() {
        categoryFactory.deleteAll();
        categoryFactory.saveAll();

        categoryTopic = categoryTopicJpaRepository.findByName("Java")
                .orElseThrow(() -> new RuntimeException("Java 토픽이 존재하지 않습니다"));
    }

    @Test
    void 전날_제출하지_않은_회원의_스트릭을_초기화한다() {
        // given
        var member1 = memberJpaRepository.save(MemberFixture.createMemberWithOutId(Role.ROLE_MEMBER));
        var streak1 = createStreakWithCount(member1);

        assertThat(streak1.getCurrentStreak()).isEqualTo(1);

        // member1은 전날 제출 안함 (createSubmission 하지 않음)

        // when
        streakScheduler.initStreakWhoDidNotSubmitYesterday();

        // then
        var updated = streakJpaRepository.findByMemberIdAndStatus(
                member1.getId(),
                EntityStatus.ACTIVE
        ).orElseThrow();

        assertThat(updated.getCurrentStreak()).isZero();
    }

    @Test
    void 전날_제출한_회원의_스트릭은_유지된다() {
        // given
        var member1 = memberJpaRepository.save(MemberFixture.createMemberWithOutId(Role.ROLE_MEMBER));
        var streak1 = createStreakWithCount(member1);

        // member1은 어제 제출함
        var yesterday = LocalDate.now().minusDays(1);
        createDailyProblemAndSubmission(member1, yesterday);

        // when
        streakScheduler.initStreakWhoDidNotSubmitYesterday();

        // then
        Streak unchanged = streakJpaRepository.findByMemberIdAndStatus(
                member1.getId(),
                EntityStatus.ACTIVE
        ).orElseThrow();

        assertThat(unchanged.getCurrentStreak()).isEqualTo(1);
    }

    @Test
    void 여러_회원_중_미제출_회원만_스트릭이_초기화된다() {
        // given
        var member1 = memberJpaRepository.save(MemberFixture.createMemberWithOutId("user1", "유저1"));
        var member2 = memberJpaRepository.save(MemberFixture.createMemberWithOutId("user2", "유저2"));
        var member3 = memberJpaRepository.save(MemberFixture.createMemberWithOutId("user3", "유저3"));

        var streak1 = createStreakWithCount(member1);
        var streak2 = createStreakWithCount(member2);
        var streak3 = createStreakWithCount(member3);

        // member1만 어제 제출
        var yesterday = LocalDate.now().minusDays(1);
        createDailyProblemAndSubmission(member1, yesterday);

        // when
        streakScheduler.initStreakWhoDidNotSubmitYesterday();

        // then
        var updated1 = streakJpaRepository.findByMemberIdAndStatus(member1.getId(), EntityStatus.ACTIVE).orElseThrow();
        var updated2 = streakJpaRepository.findByMemberIdAndStatus(member2.getId(), EntityStatus.ACTIVE).orElseThrow();
        var updated3 = streakJpaRepository.findByMemberIdAndStatus(member3.getId(), EntityStatus.ACTIVE).orElseThrow();

        assertThat(updated1.getCurrentStreak()).isEqualTo(1);
        assertThat(updated2.getCurrentStreak()).isZero();
        assertThat(updated3.getCurrentStreak()).isZero();
    }

    @Test
    void DELETED_회원은_스트릭_초기화_대상에서_제외된다() {
        // given
        var activeMember = memberJpaRepository.save(MemberFixture.createMemberWithOutId("active", "활성유저"));
        var deletedMember = memberJpaRepository.save(MemberFixture.createMemberWithOutId("deleted", "삭제유저"));

        var activeStreak = createStreakWithCount(activeMember);
        var deletedStreak = createStreakWithCount(deletedMember);

        // deletedMember를 삭제 상태로 변경
        deletedMember.delete();
        memberJpaRepository.save(deletedMember);

        // 둘 다 어제 제출 안함

        // when
        streakScheduler.initStreakWhoDidNotSubmitYesterday();

        // then
        Streak updatedActive = streakJpaRepository.findByMemberIdAndStatus(activeMember.getId(), EntityStatus.ACTIVE)
                .orElseThrow();
        Streak updatedDeleted = streakJpaRepository.findByMemberIdAndStatus(deletedMember.getId(), EntityStatus.ACTIVE)
                .orElseThrow();

        assertThat(updatedActive.getCurrentStreak()).isZero();
        assertThat(updatedDeleted.getCurrentStreak()).isEqualTo(1);
    }

    /**
     * 테스트용 Streak 생성 헬퍼 메서드
     */
    private Streak createStreakWithCount(Member member) {
        Streak streak = Streak.create(member);
        streak.increase();
        return streakJpaRepository.save(streak);
    }

    /**
     * 테스트용 DailyProblem과 Submission 생성 헬퍼 메서드
     */
    private void createDailyProblemAndSubmission(Member member, LocalDate submissionDate) {
        Problem problem = problemJpaRepository.save(Problem.create(
                "테스트 문제 " + System.nanoTime(),
                "테스트 설명",
                "AI 답변",
                categoryTopic,
                ProblemDifficulty.MEDIUM,
                submissionDate,
                "V1_PROMPT"
        ));

        DailyProblem dailyProblem = dailyProblemJpaRepository.save(
                DailyProblem.create(member, problem, submissionDate)
        );

        Submission submission = Submission.create(
                member,
                dailyProblem,
                "사용자 답변",
                submissionDate.atTime(12, 0)
        );

        submissionJpaRepository.save(submission);
    }

}
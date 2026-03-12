package org.kwakmunsu.haruhana.admin.statistics;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kwakmunsu.haruhana.IntegrationTestSupport;
import org.kwakmunsu.haruhana.admin.statistics.service.StatisticsService;
import org.kwakmunsu.haruhana.admin.statistics.service.dto.StatisticsResponse;
import org.kwakmunsu.haruhana.domain.category.CategoryFactory;
import org.kwakmunsu.haruhana.domain.category.entity.CategoryTopic;
import org.kwakmunsu.haruhana.domain.category.repository.CategoryTopicJpaRepository;
import org.kwakmunsu.haruhana.domain.dailyproblem.entity.DailyProblem;
import org.kwakmunsu.haruhana.domain.dailyproblem.repository.DailyProblemJpaRepository;
import org.kwakmunsu.haruhana.domain.member.entity.Member;
import org.kwakmunsu.haruhana.domain.member.enums.Role;
import org.kwakmunsu.haruhana.domain.member.repository.MemberJpaRepository;
import org.kwakmunsu.haruhana.domain.problem.entity.Problem;
import org.kwakmunsu.haruhana.domain.problem.enums.ProblemDifficulty;
import org.kwakmunsu.haruhana.domain.problem.repository.ProblemJpaRepository;
import org.kwakmunsu.haruhana.domain.submission.entity.Submission;
import org.kwakmunsu.haruhana.domain.submission.repository.SubmissionJpaRepository;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
class StatisticsServiceIntegrationTest extends IntegrationTestSupport {

    final StatisticsService statisticsService;

    final MemberJpaRepository memberJpaRepository;
    final ProblemJpaRepository problemJpaRepository;
    final DailyProblemJpaRepository dailyProblemJpaRepository;
    final SubmissionJpaRepository submissionJpaRepository;
    final CategoryTopicJpaRepository categoryTopicJpaRepository;
    final CategoryFactory categoryFactory;
    final EntityManager entityManager;

    @BeforeEach
    void setUp() {
        submissionJpaRepository.deleteAll();
        dailyProblemJpaRepository.deleteAll();
        problemJpaRepository.deleteAll();
        memberJpaRepository.deleteAll();
        categoryFactory.deleteAll();
        categoryFactory.saveAll();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void 회원수를_정확하게_반환한다() {
        // given
        memberJpaRepository.save(createMember("user1", "nick1"));
        memberJpaRepository.save(createMember("user2", "nick2"));
        memberJpaRepository.save(createMember("user3", "nick3"));
        entityManager.flush();
        entityManager.clear();

        // when
        StatisticsResponse result = statisticsService.getStatistics();

        // then
        assertThat(result.totalMemberCount()).isEqualTo(3);
    }

    @Test
    void 오늘_생성된_문제수를_정확하게_반환한다() {
        // given
        var topic = categoryTopicJpaRepository.findByName("Java").orElseThrow();

        problemJpaRepository.save(createProblem("오늘의 문제1", topic, LocalDate.now()));
        problemJpaRepository.save(createProblem("오늘의 문제2", topic, LocalDate.now()));
        problemJpaRepository.save(createProblem("어제의 문제", topic, LocalDate.now().minusDays(1)));
        entityManager.flush();
        entityManager.clear();

        // when
        StatisticsResponse result = statisticsService.getStatistics();

        // then
        assertThat(result.todayProblemCount()).isEqualTo(2);
    }

    @Test
    void 어제_생성된_문제는_오늘_통계에_포함되지_않는다() {
        // given
        var topic = categoryTopicJpaRepository.findByName("Java").orElseThrow();

        problemJpaRepository.save(createProblem("어제 문제", topic, LocalDate.now().minusDays(1)));
        entityManager.flush();
        entityManager.clear();

        // when
        StatisticsResponse result = statisticsService.getStatistics();

        // then
        assertThat(result.todayProblemCount()).isZero();
    }

    @Test
    void 오늘_제시간_제출수를_정확하게_반환한다() {
        // given
        var topic = categoryTopicJpaRepository.findByName("Java").orElseThrow();
        var member1 = memberJpaRepository.save(createMember("user1", "nick1"));
        var member2 = memberJpaRepository.save(createMember("user2", "nick2"));
        var member3 = memberJpaRepository.save(createMember("user3", "nick3"));

        Problem problem = problemJpaRepository.save(createProblem("오늘 문제", topic, LocalDate.now()));

        // 오늘 날짜의 DailyProblem
        var dp1 = dailyProblemJpaRepository.save(DailyProblem.create(member1, problem, LocalDate.now()));
        var dp2 = dailyProblemJpaRepository.save(DailyProblem.create(member2, problem, LocalDate.now()));
        // 어제 날짜의 DailyProblem (늦은 제출)
        var dp3 = dailyProblemJpaRepository.save(DailyProblem.create(member3, problem, LocalDate.now().minusDays(1)));

        entityManager.flush();
        entityManager.clear();

        // dp1, dp2: 오늘 제시간 제출 (isOnTime=true)
        submissionJpaRepository.save(Submission.create(member1, dp1, "답변1", LocalDateTime.now()));
        submissionJpaRepository.save(Submission.create(member2, dp2, "답변2", LocalDateTime.now()));
        // dp3: 늦은 제출 (isOnTime=false, assignedAt이 어제이므로 오늘 제출하면 지각)
        submissionJpaRepository.save(Submission.create(member3, dp3, "답변3", LocalDateTime.now()));
        entityManager.flush();
        entityManager.clear();

        // when
        StatisticsResponse result = statisticsService.getStatistics();

        // then: 오늘 날짜 DailyProblem에 isOnTime=true인 건만 카운트 → 2
        assertThat(result.todayOnTimeSubmissionCount()).isEqualTo(2);
    }

    @Test
    void 데이터가_없으면_모든_통계가_0이다() {
        // given: setUp에서 전체 삭제됨

        // when
        StatisticsResponse result = statisticsService.getStatistics();

        // then
        assertThat(result.totalMemberCount()).isZero();
        assertThat(result.todayProblemCount()).isZero();
        assertThat(result.todayOnTimeSubmissionCount()).isZero();
    }

    @Test
    void 세_가지_통계가_모두_정확히_반환된다() {
        // given
        var topic = categoryTopicJpaRepository.findByName("Python").orElseThrow();
        var member = memberJpaRepository.save(createMember("user1", "nick1"));

        var problem = problemJpaRepository.save(createProblem("오늘 문제", topic, LocalDate.now()));
        var dp = dailyProblemJpaRepository.save(DailyProblem.create(member, problem, LocalDate.now()));
        entityManager.flush();
        entityManager.clear();

        submissionJpaRepository.save(Submission.create(member, dp, "답변", LocalDateTime.now()));
        entityManager.flush();
        entityManager.clear();

        // when
        StatisticsResponse result = statisticsService.getStatistics();

        // then
        assertThat(result.totalMemberCount()).isEqualTo(1);
        assertThat(result.todayProblemCount()).isEqualTo(1);
        assertThat(result.todayOnTimeSubmissionCount()).isEqualTo(1);
    }

    // ──────────────────────── 헬퍼 메서드 ────────────────────────

    private Member createMember(String loginId, String nickname) {
        return Member.createMember(loginId, "password1234!", nickname, Role.ROLE_MEMBER);
    }

    private Problem createProblem(String title, CategoryTopic topic, LocalDate date) {
        return Problem.create(title, "설명", "AI 답변", topic, ProblemDifficulty.EASY, date, "V1_PROMPT");
    }

}
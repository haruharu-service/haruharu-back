package org.kwakmunsu.haruhana.domain.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
import org.kwakmunsu.haruhana.domain.problem.entity.Problem;
import org.kwakmunsu.haruhana.domain.problem.enums.ProblemDifficulty;
import org.kwakmunsu.haruhana.domain.problem.repository.ProblemJpaRepository;
import org.kwakmunsu.haruhana.domain.submission.entity.Submission;
import org.kwakmunsu.haruhana.domain.submission.repository.SubmissionJpaRepository;
import org.kwakmunsu.haruhana.global.entity.EntityStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
class MemberJpaRepositoryTest extends IntegrationTestSupport {

    final CategoryFactory categoryFactory;
    final MemberJpaRepository memberJpaRepository;
    final CategoryTopicJpaRepository categoryTopicJpaRepository;
    final ProblemJpaRepository problemJpaRepository;
    final DailyProblemJpaRepository dailyProblemJpaRepository;
    final SubmissionJpaRepository submissionJpaRepository;

    CategoryTopic categoryTopic;
    LocalDateTime startOfToday;
    LocalDateTime endOfToday;

    @BeforeEach
    void setUp() {
        categoryFactory.deleteAll();
        categoryFactory.saveAll();

        categoryTopic = categoryTopicJpaRepository.findByName("Java")
                .orElseThrow(() -> new RuntimeException("Java 토픽이 존재하지 않습니다"));

        startOfToday = LocalDate.now().atStartOfDay();
        endOfToday = LocalDate.now().plusDays(1).atStartOfDay();
    }

    @Test
    void 오늘_제출하지_않은_회원만_조회한다() {
        // given
        var member1 = memberJpaRepository.save(MemberFixture.createMemberWithOutId("user1", "유저1"));
        var member2 = memberJpaRepository.save(MemberFixture.createMemberWithOutId("user2", "유저2"));
        var member3 = memberJpaRepository.save(MemberFixture.createMemberWithOutId("user3", "유저3"));

        // member1은 오늘 제출함
        createSubmission(member1, LocalDateTime.now());

        // member2는 어제 제출함
        createSubmission(member2, LocalDateTime.now().minusDays(1));

        // member3은 제출 안함

        // when
        List<Member> result = memberJpaRepository.findMembersWithoutTodaySubmission(
                startOfToday,
                endOfToday,
                Role.ROLE_MEMBER,
                EntityStatus.ACTIVE
        );

        // then
        assertThat(result).hasSize(2)
                .extracting(Member::getLoginId)
                .containsExactlyInAnyOrder("user2", "user3");
    }

    @Test
    void INACTIVE_상태의_회원은_조회하지_않는다() {
        // given
        var activeMember = memberJpaRepository.save(MemberFixture.createMemberWithOutId("active", "활성유저"));
        var inactiveMember = MemberFixture.createMemberWithOutId("inactive", "비활성유저");
        // inactiveMember를 비활성화
        ReflectionTestUtils.setField(inactiveMember, "status", EntityStatus.DELETED);
        memberJpaRepository.save(inactiveMember);

        // when
        List<Member> result = memberJpaRepository.findMembersWithoutTodaySubmission(
                startOfToday,
                endOfToday,
                Role.ROLE_MEMBER,
                EntityStatus.ACTIVE
        );

        // then
        assertThat(result).hasSize(1)
                .extracting(Member::getLoginId)
                .containsExactly(activeMember.getLoginId());
    }

    @Test
    void MEMBER_권한의_회원만_조회한다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.createMemberWithOutId("member", "멤버"));

        // when
        List<Member> result = memberJpaRepository.findMembersWithoutTodaySubmission(
                startOfToday,
                endOfToday,
                Role.ROLE_MEMBER,
                EntityStatus.ACTIVE
        );

        // then
        assertThat(result).hasSize(1)
                .extracting(Member::getLoginId)
                .containsExactly(member.getLoginId());
    }

    @Test
    void 모든_회원이_제출한_경우_빈_리스트를_반환한다() {
        // given
        var member1 = memberJpaRepository.save(MemberFixture.createMemberWithOutId("user1", "유저1"));
        var member2 = memberJpaRepository.save(MemberFixture.createMemberWithOutId("user2", "유저2"));

        // 모두 오늘 제출
        createSubmission(member1, LocalDateTime.now());
        createSubmission(member2, LocalDateTime.now());

        // when
        List<Member> result = memberJpaRepository.findMembersWithoutTodaySubmission(
                startOfToday,
                endOfToday,
                Role.ROLE_MEMBER,
                EntityStatus.ACTIVE
        );

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 회원이_없으면_빈_리스트를_반환한다() {
        // given - 회원이 없음

        // when
        List<Member> result = memberJpaRepository.findMembersWithoutTodaySubmission(
                startOfToday,
                endOfToday,
                Role.ROLE_MEMBER,
                EntityStatus.ACTIVE
        );

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 시간대가_다른_오늘_제출도_정확히_필터링된다() {
        // given
        var member1 = memberJpaRepository.save(MemberFixture.createMemberWithOutId("user1", "유저1"));
        var member2 = memberJpaRepository.save(MemberFixture.createMemberWithOutId("user2", "유저2"));
        var member3 = memberJpaRepository.save(MemberFixture.createMemberWithOutId("user3", "유저3"));

        // member1: 오늘 새벽 00:01
        createSubmission(member1, LocalDate.now().atTime(0, 1));

        // member2: 오늘 저녁 23:59
        createSubmission(member2, LocalDate.now().atTime(23, 59));

        // member3: 제출 안함

        // when
        List<Member> result = memberJpaRepository.findMembersWithoutTodaySubmission(
                startOfToday,
                endOfToday,
                Role.ROLE_MEMBER,
                EntityStatus.ACTIVE
        );

        // then - member3만 조회됨
        assertThat(result).hasSize(1)
                .extracting(Member::getLoginId)
                .containsExactly(member3.getLoginId());
    }

    /**
     * 테스트용 제출 생성 헬퍼 메서드
     */
    private Submission createSubmission(Member member, LocalDateTime submittedAt) {
        Problem problem = problemJpaRepository.save(Problem.create(
                "테스트 문제",
                "테스트 설명",
                "AI 답변",
                categoryTopic,
                ProblemDifficulty.MEDIUM,
                LocalDate.now(),
                "V1_PROMPT"
        ));

        DailyProblem dailyProblem = dailyProblemJpaRepository.save(
                DailyProblem.create(member, problem, LocalDate.now())
        );

        Submission submission = Submission.create(
                member,
                dailyProblem,
                "사용자 답변",
                submittedAt
        );
        return submissionJpaRepository.save(submission);
    }

}
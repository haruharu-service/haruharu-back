package org.kwakmunsu.haruhana.admin.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.kwakmunsu.haruhana.UnitTestSupport;
import org.kwakmunsu.haruhana.admin.member.enums.SortBy;
import org.kwakmunsu.haruhana.admin.member.service.dto.AdminMemberPreferenceResponse;
import org.kwakmunsu.haruhana.admin.member.service.dto.AdminMemberPreviewResponse;
import org.kwakmunsu.haruhana.domain.category.entity.CategoryTopic;
import org.kwakmunsu.haruhana.domain.member.MemberFixture;
import org.kwakmunsu.haruhana.domain.member.entity.Member;
import org.kwakmunsu.haruhana.domain.member.entity.MemberPreference;
import org.kwakmunsu.haruhana.domain.member.enums.Role;
import org.kwakmunsu.haruhana.domain.member.repository.MemberPreferenceJpaRepository;
import org.kwakmunsu.haruhana.domain.member.repository.MemberQueryDslRepository;
import org.kwakmunsu.haruhana.domain.problem.enums.ProblemDifficulty;
import org.kwakmunsu.haruhana.global.entity.EntityStatus;
import org.kwakmunsu.haruhana.global.support.OffsetLimit;
import org.kwakmunsu.haruhana.global.support.error.ErrorType;
import org.kwakmunsu.haruhana.global.support.error.HaruHanaException;
import org.kwakmunsu.haruhana.global.support.response.PageResponse;
import org.kwakmunsu.haruhana.util.TestDateTimeUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class AdminMemberReaderUnitTest extends UnitTestSupport {

    @Mock
    MemberQueryDslRepository memberQueryDslRepository;

    @Mock
    MemberPreferenceJpaRepository memberPreferenceJpaRepository;

    @InjectMocks
    AdminMemberReader adminMemberReader;

    @Test
    void 다음_페이지가_존재하는_경우로_회원_정보를_조회한다() {
        // given
        List<Member> members = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            members.add(MemberFixture.createMemberWithOutId(
                    "user" + i,
                    "유저" + i
            ));
        }

        given(memberQueryDslRepository.findMembers(any(), any(), any())).willReturn(members);

        // when
        PageResponse<AdminMemberPreviewResponse> response = adminMemberReader.findMembers(
                null,
                SortBy.JOIN_DESC,
                new OffsetLimit(1, 2)
        );

        // then
        assertThat(response.hasNext()).isTrue();
        assertThat(response.contents()).hasSize(2);
    }

    @Test
    void 미지막_페이지의_회원_정보를_조회한다() {
        // given
        List<Member> members = List.of(
                MemberFixture.createMemberWithOutId("user2", "유저2"),
                MemberFixture.createMemberWithOutId("user3", "유저3"),
                MemberFixture.createMemberWithOutId("user1", "유저1")
        );
        given(memberQueryDslRepository.findMembers(any(), any(), any())).willReturn(members);

        // when
        PageResponse<AdminMemberPreviewResponse> response = adminMemberReader.findMembers(
                null,
                SortBy.JOIN_DESC,
                new OffsetLimit(1, 20)
        );

        // then
        assertThat(response.hasNext()).isFalse();
        assertThat(response.contents()).extracting(
                AdminMemberPreviewResponse::loginId,
                AdminMemberPreviewResponse::nickname
        ).containsExactly(
                tuple("user2", "유저2"),
                tuple("user3", "유저3"),
                tuple("user1", "유저1")
        );
    }

    @Test
    void 조회된_회원_정보를_확인한다() {
        // given
        List<Member> members = List.of(MemberFixture.createMemberWithOutId("user2", "유저2"));
        given(memberQueryDslRepository.findMembers(any(), any(), any())).willReturn(members);

        // when
        PageResponse<AdminMemberPreviewResponse> response = adminMemberReader.findMembers(
                null,
                SortBy.JOIN_DESC,
                new OffsetLimit(1, 20)
        );

        // then
        assertThat(response.hasNext()).isFalse();
        assertThat(response.contents()).extracting(
                AdminMemberPreviewResponse::loginId,
                AdminMemberPreviewResponse::nickname,
                AdminMemberPreviewResponse::role,
                AdminMemberPreviewResponse::status
        ).containsExactly(
                tuple("user2", "유저2", Role.ROLE_MEMBER, EntityStatus.ACTIVE)
        );
    }

    @Test
    void 회원_학습_정보를_조회한다() {
        // given
        LocalDate effectiveAt = TestDateTimeUtils.now().toLocalDate();
        var member = mock(Member.class);
        given(member.getId()).willReturn(1L);

        var spring = CategoryTopic.create(1L, "Spring");
        var mockedPreference = mock(MemberPreference.class);

        given(mockedPreference.getId()).willReturn(1L);
        given(mockedPreference.getMember()).willReturn(member);
        given(mockedPreference.getCategoryTopic()).willReturn(spring);
        given(mockedPreference.getDifficulty()).willReturn(ProblemDifficulty.EASY);
        given(mockedPreference.getEffectiveAt()).willReturn(effectiveAt);

        given(memberPreferenceJpaRepository.findByMemberIdAndStatus(any(), any())).willReturn(Optional.of(mockedPreference));

        // when
        var adminMemberReaderMemberPreference = adminMemberReader.findMemberPreference(1L);

        // then
        assertThat(adminMemberReaderMemberPreference).isNotNull()
                .extracting(
                        AdminMemberPreferenceResponse::memberId,
                        AdminMemberPreferenceResponse::categoryTopic,
                        AdminMemberPreferenceResponse::difficulty,
                        AdminMemberPreferenceResponse::effectiveAt
                ).containsExactly(
                        1L,
                        spring.getName(),
                        ProblemDifficulty.EASY.name(),
                        effectiveAt
                );
    }

    @Test
    void 회원_학습_정보가_없을_시_예외를_반환한다() {
        given(memberPreferenceJpaRepository.findByMemberIdAndStatus(any(), any())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminMemberReader.findMemberPreference(1L))
                .isInstanceOf(HaruHanaException.class)
                .hasMessage(ErrorType.NOT_FOUND_MEMBER_PREFERENCE.getMessage());
    }

}
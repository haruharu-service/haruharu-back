package org.kwakmunsu.haruhana.domain.member;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.kwakmunsu.haruhana.domain.member.controller.dto.MemberCreateRequest;
import org.kwakmunsu.haruhana.domain.member.entity.Member;
import org.kwakmunsu.haruhana.domain.member.enums.Role;
import org.kwakmunsu.haruhana.domain.member.service.dto.request.NewPreference;
import org.kwakmunsu.haruhana.domain.member.service.dto.request.NewProfile;
import org.kwakmunsu.haruhana.domain.problem.enums.ProblemDifficulty;
import org.springframework.test.util.ReflectionTestUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MemberFixture {

    public static final String LOGIN_ID = "testLoginId";
    public static final String NICKNAME = "testNickname";
    public static final String PASSWORD = "testPassword1234!";
    public static final Long CATEGORY_ID = 1L;

    public static MemberCreateRequest createMemberCreateRequest() {
        return new MemberCreateRequest(
                LOGIN_ID,
                PASSWORD,
                NICKNAME,
                CATEGORY_ID,
                ProblemDifficulty.MEDIUM
        );
    }

    public static NewProfile createNewProfile() {
        return new NewProfile(
                LOGIN_ID,
                PASSWORD,
                NICKNAME
        );
    }

    public static Member createMember(Role role) {
        var member = Member.createMember(
                LOGIN_ID,
                PASSWORD,
                NICKNAME,
                role
        );
        ReflectionTestUtils.setField(member, "id", 1L);

        return member;
    }

    public static Member createMemberWithOutId(String loginId, String nickname) {
        return Member.createMember(
                loginId,
                PASSWORD,
                nickname,
                Role.ROLE_MEMBER
        );
    }

    public static Member createMemberWithOutId(Role role) {
        return Member.createMember(
                LOGIN_ID,
                PASSWORD,
                NICKNAME,
                role
        );
    }

    public static NewPreference createNewPreference(Long categoryTopicId) {
        return new NewPreference(categoryTopicId, ProblemDifficulty.MEDIUM);
    }

}
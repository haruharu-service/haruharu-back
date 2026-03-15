package org.kwakmunsu.haruhana.domain.member.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.kwakmunsu.haruhana.domain.member.MemberFixture;
import org.kwakmunsu.haruhana.domain.member.enums.Role;

class MemberTest {

    @Test
    void 익명화_시_loginId와_nickname이_deleted_형식으로_변경된다() {
        // given
        Member member = MemberFixture.createMember(Role.ROLE_MEMBER);

        // when
        member.anonymize();

        // then
        assertThat(member.getLoginId()).startsWith("deleted_");
        assertThat(member.getNickname()).startsWith("deleted_");
    }

    @Test
    void 익명화_후_loginId와_nickname은_동일한_값이다() {
        // given
        Member member = MemberFixture.createMember(Role.ROLE_MEMBER);

        // when
        member.anonymize();

        // then
        assertThat(member.getLoginId()).isEqualTo(member.getNickname());
    }

    @Test
    void 익명화_시_기존_loginId와_nickname이_다른_값으로_변경된다() {
        // given
        Member member = MemberFixture.createMember(Role.ROLE_MEMBER);
        String originalLoginId = member.getLoginId();
        String originalNickname = member.getNickname();

        // when
        member.anonymize();

        // then
        assertThat(member.getLoginId()).isNotEqualTo(originalLoginId);
        assertThat(member.getNickname()).isNotEqualTo(originalNickname);
    }

    @Test
    void 익명화를_두_번_호출하면_다른_값이_생성된다() {
        // given
        Member member1 = MemberFixture.createMember(Role.ROLE_MEMBER);
        Member member2 = MemberFixture.createMember(Role.ROLE_MEMBER);

        // when
        member1.anonymize();
        member2.anonymize();

        // then
        assertThat(member1.getLoginId()).isNotEqualTo(member2.getLoginId());
        assertThat(member1.getNickname()).isNotEqualTo(member2.getNickname());
    }

}

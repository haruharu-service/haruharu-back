package org.kwakmunsu.haruhana.admin.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.kwakmunsu.haruhana.IntegrationTestSupport;
import org.kwakmunsu.haruhana.domain.member.MemberFixture;
import org.kwakmunsu.haruhana.domain.member.enums.Role;
import org.kwakmunsu.haruhana.domain.member.repository.MemberJpaRepository;
import org.kwakmunsu.haruhana.global.support.error.ErrorType;
import org.kwakmunsu.haruhana.global.support.error.HaruHanaException;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
class AdminMemberManagerIntegrationTest extends IntegrationTestSupport {

    final MemberJpaRepository memberJpaRepository;
    final AdminMemberManager adminMemberManager;
    final EntityManager entityManager;

    @Test
    void 관리자가_회원_권한을_MEMBER에서_ADMIN으로변경한다() {
        // given
        var member = MemberFixture.createMemberWithOutId(Role.ROLE_MEMBER);
        memberJpaRepository.save(member);

        // when
        adminMemberManager.updateMemberRole(member.getId(), Role.ROLE_ADMIN);
        entityManager.flush();

        // then
        var updatedMember = memberJpaRepository.findById(member.getId()).orElseThrow();

        assertThat(updatedMember.getRole()).isEqualTo(Role.ROLE_ADMIN);
    }

    @Test
    void 관리자가_회원_권한을_ADMIN에서_MEMBER으로변경한다() {
        // given
        var admin = MemberFixture.createMemberWithOutId(Role.ROLE_ADMIN);
        memberJpaRepository.save(admin);

        // when
        adminMemberManager.updateMemberRole(admin.getId(), Role.ROLE_MEMBER);
        entityManager.flush();

        // then
        var updatedMember = memberJpaRepository.findById(admin.getId()).orElseThrow();

        assertThat(updatedMember.getRole()).isEqualTo(Role.ROLE_MEMBER);
    }

    @Test
    void 존재하지_않는_회원의_권한을_변경하면_예외를_반환한다() {
        // given
        var nonExistentMemberId = 999999L;

        // when & then
        assertThatThrownBy(() -> adminMemberManager.updateMemberRole(nonExistentMemberId, Role.ROLE_ADMIN))
                .isInstanceOf(HaruHanaException.class)
                .hasMessage(ErrorType.NOT_FOUND_MEMBER.getMessage());
    }

    @Test
    void 관리자가_회원을_비활성화한다() {
        // given
        var member = MemberFixture.createMemberWithOutId(Role.ROLE_ADMIN);
        memberJpaRepository.save(member);

        // when
        adminMemberManager.deleteMember(member.getId());
        entityManager.flush();

        // then
        var deletedMember = memberJpaRepository.findById(member.getId()).orElseThrow();

        assertThat(deletedMember.isDeleted()).isTrue();
    }

    @Test
    void 이미_비활성화된_회원이라면_아무_일도_일어나지_않는다() {
        // given
        var member = MemberFixture.createMemberWithOutId(Role.ROLE_ADMIN);
        memberJpaRepository.save(member);
        adminMemberManager.deleteMember(member.getId());
        entityManager.flush();

        // when -  update 쿼리가 안나간다면 아무 일도 일어나지 않음.
        adminMemberManager.deleteMember(member.getId());
        entityManager.flush();
    }

    @Test
    void 관리자가_회원_닉네임을_변경한다() {
        // given
        var member = MemberFixture.createMemberWithOutId("loginId1", "기존닉네임");
        memberJpaRepository.save(member);

        // when
        adminMemberManager.updateMemberNickname(member.getId(), "변경닉네임");
        entityManager.flush();

        // then
        var updatedMember = memberJpaRepository.findById(member.getId()).orElseThrow();
        assertThat(updatedMember.getNickname()).isEqualTo("변경닉네임");
    }

    @Test
    void 현재_닉네임과_동일한_닉네임으로_변경하면_변경_없이_처리된다() {
        // given
        var member = MemberFixture.createMemberWithOutId(Role.ROLE_MEMBER);
        memberJpaRepository.save(member);

        // when
        adminMemberManager.updateMemberNickname(member.getId(), MemberFixture.NICKNAME);
        entityManager.flush();

        // then
        var updatedMember = memberJpaRepository.findById(member.getId()).orElseThrow();
        assertThat(updatedMember.getNickname()).isEqualTo(MemberFixture.NICKNAME);
    }

    @Test
    void 이미_사용_중인_닉네임으로_변경하면_예외를_반환한다() {
        // given
        var member1 = MemberFixture.createMemberWithOutId("loginId1", "닉네임1");
        var member2 = MemberFixture.createMemberWithOutId("loginId2", "닉네임2");
        memberJpaRepository.saveAll(List.of(member1, member2));
        entityManager.flush();

        // when & then
        assertThatThrownBy(() -> adminMemberManager.updateMemberNickname(member2.getId(), "닉네임1"))
                .isInstanceOf(HaruHanaException.class)
                .hasMessage(ErrorType.DUPLICATE_NICKNAME.getMessage());
    }

    @Test
    void 존재하지_않는_회원의_닉네임을_변경하면_예외를_반환한다() {
        // given
        var nonExistentMemberId = 999999L;

        // when & then
        assertThatThrownBy(() -> adminMemberManager.updateMemberNickname(nonExistentMemberId, "변경닉네임"))
                .isInstanceOf(HaruHanaException.class)
                .hasMessage(ErrorType.NOT_FOUND_MEMBER.getMessage());
    }

}
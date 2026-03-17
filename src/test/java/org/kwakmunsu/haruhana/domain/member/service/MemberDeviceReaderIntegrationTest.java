package org.kwakmunsu.haruhana.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.kwakmunsu.haruhana.IntegrationTestSupport;
import org.kwakmunsu.haruhana.domain.member.MemberFixture;
import org.kwakmunsu.haruhana.domain.member.entity.MemberDevice;
import org.kwakmunsu.haruhana.domain.member.enums.Role;
import org.kwakmunsu.haruhana.domain.member.repository.MemberDeviceJpaRepository;
import org.kwakmunsu.haruhana.domain.member.repository.MemberJpaRepository;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
class MemberDeviceReaderIntegrationTest extends IntegrationTestSupport {

    final MemberJpaRepository memberJpaRepository;
    final MemberDeviceJpaRepository memberDeviceJpaRepository;
    final MemberDeviceReader memberDeviceReader;

    @Test
    void 여러_회원_ID의_ACTIVE_디바이스_토큰을_한_번에_조회한다() {
        // given
        var member1 = MemberFixture.createMemberWithOutId(Role.ROLE_MEMBER);
        var member2 = MemberFixture.createMemberWithOutId(Role.ROLE_MEMBER);
        memberJpaRepository.saveAll(List.of(member1, member2));

        memberDeviceJpaRepository.save(MemberDevice.register(member1, "token-member1", LocalDateTime.now()));
        memberDeviceJpaRepository.save(MemberDevice.register(member2, "token-member2", LocalDateTime.now()));

        // when
        List<String> tokens = memberDeviceReader.findDeviceTokensByMemberIds(
                List.of(member1.getId(), member2.getId())
        );

        // then
        assertThat(tokens).containsExactlyInAnyOrder("token-member1", "token-member2");
    }

    @Test
    void 한_회원이_여러_디바이스를_가진_경우_모두_조회된다() {
        // given
        var member = MemberFixture.createMemberWithOutId(Role.ROLE_MEMBER);
        memberJpaRepository.save(member);

        memberDeviceJpaRepository.save(MemberDevice.register(member, "token-device-1", LocalDateTime.now()));
        memberDeviceJpaRepository.save(MemberDevice.register(member, "token-device-2", LocalDateTime.now()));

        // when
        List<String> tokens = memberDeviceReader.findDeviceTokensByMemberIds(List.of(member.getId()));

        // then
        assertThat(tokens).containsExactlyInAnyOrder("token-device-1", "token-device-2");
    }

    @Test
    void DELETED_상태의_디바이스_토큰은_조회되지_않는다() {
        // given
        var member = MemberFixture.createMemberWithOutId(Role.ROLE_MEMBER);
        memberJpaRepository.save(member);

        MemberDevice activeDevice = MemberDevice.register(member, "active-token", LocalDateTime.now());
        MemberDevice deletedDevice = MemberDevice.register(member, "deleted-token", LocalDateTime.now());
        memberDeviceJpaRepository.saveAll(List.of(activeDevice, deletedDevice));

        deletedDevice.delete();

        // when
        List<String> tokens = memberDeviceReader.findDeviceTokensByMemberIds(List.of(member.getId()));

        // then
        assertThat(tokens).containsExactly("active-token");
        assertThat(tokens).doesNotContain("deleted-token");
    }

    @Test
    void 조회_대상_회원_ID에_포함되지_않은_회원의_토큰은_반환되지_않는다() {
        // given
        var targetMember = MemberFixture.createMemberWithOutId(Role.ROLE_MEMBER);
        var otherMember = MemberFixture.createMemberWithOutId(Role.ROLE_MEMBER);
        memberJpaRepository.saveAll(List.of(targetMember, otherMember));

        memberDeviceJpaRepository.save(MemberDevice.register(targetMember, "target-token", LocalDateTime.now()));
        memberDeviceJpaRepository.save(MemberDevice.register(otherMember, "other-token", LocalDateTime.now()));

        // when
        List<String> tokens = memberDeviceReader.findDeviceTokensByMemberIds(List.of(targetMember.getId()));

        // then
        assertThat(tokens).containsExactly("target-token");
        assertThat(tokens).doesNotContain("other-token");
    }

}
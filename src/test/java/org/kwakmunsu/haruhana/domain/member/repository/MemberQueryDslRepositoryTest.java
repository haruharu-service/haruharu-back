package org.kwakmunsu.haruhana.domain.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.kwakmunsu.haruhana.IntegrationTestSupport;
import org.kwakmunsu.haruhana.admin.member.enums.SortBy;
import org.kwakmunsu.haruhana.domain.member.MemberFixture;
import org.kwakmunsu.haruhana.domain.member.entity.Member;
import org.kwakmunsu.haruhana.global.support.OffsetLimit;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
class MemberQueryDslRepositoryTest extends IntegrationTestSupport {

    final MemberQueryDslRepository memberQueryDslRepository;
    final MemberJpaRepository memberJpaRepository;

    // ======== 검색 조건 테스트 ========

    @Test
    void 닉네임에_검색어가_포함된_회원만_조회된다() {
        // given
        memberJpaRepository.save(MemberFixture.createMemberWithOutId("user1", "자바개발자"));
        memberJpaRepository.save(MemberFixture.createMemberWithOutId("user2", "파이썬개발자"));
        memberJpaRepository.save(MemberFixture.createMemberWithOutId("user3", "스프링개발자"));

        // when
        List<Member> result = memberQueryDslRepository.findMembers("자바", null, new OffsetLimit(1, 20));

        // then
        assertThat(result).hasSize(1)
                .extracting(Member::getNickname)
                .containsExactly("자바개발자");
    }

    @Test
    void 아이디에_검색어가_포함된_회원도_조회된다() {
        // given
        memberJpaRepository.save(MemberFixture.createMemberWithOutId("javaUser", "유저1"));
        memberJpaRepository.save(MemberFixture.createMemberWithOutId("pythonUser", "유저2"));

        // when
        List<Member> result = memberQueryDslRepository.findMembers("java", null, new OffsetLimit(1, 20));

        // then
        assertThat(result).hasSize(1)
                .extracting(Member::getLoginId)
                .containsExactly("javaUser");
    }

    @Test
    void 검색은_대소문자를_구분하지_않는다() {
        // given
        memberJpaRepository.save(MemberFixture.createMemberWithOutId("javaUser", "유저1"));
        memberJpaRepository.save(MemberFixture.createMemberWithOutId("pythonUser", "유저2"));

        // when
        List<Member> result = memberQueryDslRepository.findMembers("JAVA", null, new OffsetLimit(1, 20));

        // then
        assertThat(result).hasSize(1)
                .extracting(Member::getLoginId)
                .containsExactly("javaUser");
    }

    @Test
    void 검색어가_빈_문자열이면_전체_회원을_조회한다() {
        // given
        memberJpaRepository.save(MemberFixture.createMemberWithOutId("user1", "유저1"));
        memberJpaRepository.save(MemberFixture.createMemberWithOutId("user2", "유저2"));
        memberJpaRepository.save(MemberFixture.createMemberWithOutId("user3", "유저3"));

        // when
        List<Member> result = memberQueryDslRepository.findMembers("", null, new OffsetLimit(1, 20));

        // then
        assertThat(result).hasSize(3);
    }

    @Test
    void 검색어가_공백만_있으면_전체_회원을_조회한다() {
        // given
        memberJpaRepository.save(MemberFixture.createMemberWithOutId("user1", "유저1"));
        memberJpaRepository.save(MemberFixture.createMemberWithOutId("user2", "유저2"));

        // when
        List<Member> result = memberQueryDslRepository.findMembers("   ", null, new OffsetLimit(1, 20));

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void 검색어가_null이면_전체_회원을_조회한다() {
        // given
        memberJpaRepository.save(MemberFixture.createMemberWithOutId("user1", "유저1"));
        memberJpaRepository.save(MemberFixture.createMemberWithOutId("user2", "유저2"));

        // when
        List<Member> result = memberQueryDslRepository.findMembers(null, null, new OffsetLimit(1, 20));

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void 검색어와_일치하는_회원이_없으면_빈_리스트를_반환한다() {
        // given
        memberJpaRepository.save(MemberFixture.createMemberWithOutId("user1", "유저1"));

        // when
        List<Member> result = memberQueryDslRepository.findMembers("존재하지않는검색어", null, new OffsetLimit(1, 20));

        // then
        assertThat(result).isEmpty();
    }

    // ======== 정렬 테스트 ========

    @Test
    void 정렬_기준이_null이면_id_내림차순으로_조회된다() {
        // given
        var member1 = memberJpaRepository.save(MemberFixture.createMemberWithOutId("user1", "유저1"));
        var member2 = memberJpaRepository.save(MemberFixture.createMemberWithOutId("user2", "유저2"));
        var member3 = memberJpaRepository.save(MemberFixture.createMemberWithOutId("user3", "유저3"));

        // when
        List<Member> result = memberQueryDslRepository.findMembers("", null, new OffsetLimit(1, 20));

        // then
        assertThat(result).extracting(Member::getId)
                .containsExactly(
                        member3.getId(),
                        member2.getId(),
                        member1.getId()
                );
    }

    @Test
    void JOIN_ASC이면_id_오름차순으로_조회된다() {
        // given
        var member1 = memberJpaRepository.save(MemberFixture.createMemberWithOutId("user1", "유저1"));
        var member2 = memberJpaRepository.save(MemberFixture.createMemberWithOutId("user2", "유저2"));
        var member3 = memberJpaRepository.save(MemberFixture.createMemberWithOutId("user3", "유저3"));

        // when
        List<Member> result = memberQueryDslRepository.findMembers("", SortBy.JOIN_ASC, new OffsetLimit(1, 20));

        // then
        assertThat(result).extracting(Member::getId)
                .containsExactly(member1.getId(), member2.getId(), member3.getId());
    }

    @Test
    void JOIN_DESC이면_id_내림차순으로_조회된다() {
        // given
        var member1 = memberJpaRepository.save(MemberFixture.createMemberWithOutId("user1", "유저1"));
        var member2 = memberJpaRepository.save(MemberFixture.createMemberWithOutId("user2", "유저2"));
        var member3 = memberJpaRepository.save(MemberFixture.createMemberWithOutId("user3", "유저3"));

        // when
        List<Member> result = memberQueryDslRepository.findMembers("", SortBy.JOIN_DESC, new OffsetLimit(1, 20));

        // then
        assertThat(result).extracting(Member::getId)
                .containsExactly(member3.getId(), member2.getId(), member1.getId());
    }

    // ======== 페이지네이션 테스트 ========

    @Test
    void 다음_페이지_감지를_위해_size보다_1개_더_조회한다() {
        // given - size=2 이면 limit+1=3 개를 조회
        memberJpaRepository.save(MemberFixture.createMemberWithOutId("user1", "유저1"));
        memberJpaRepository.save(MemberFixture.createMemberWithOutId("user2", "유저2"));
        memberJpaRepository.save(MemberFixture.createMemberWithOutId("user3", "유저3"));

        // when
        List<Member> result = memberQueryDslRepository.findMembers("", null, new OffsetLimit(1, 2));

        // then
        assertThat(result).hasSize(3);
    }

    @Test
    void 다음_페이지가_없으면_size_이하로_조회된다() {
        // given
        memberJpaRepository.save(MemberFixture.createMemberWithOutId("user1", "유저1"));
        memberJpaRepository.save(MemberFixture.createMemberWithOutId("user2", "유저2"));

        // when - size=5 이면 limit+1=6 이지만 데이터는 2개
        List<Member> result = memberQueryDslRepository.findMembers("", null, new OffsetLimit(1, 5));

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void offset이_적용되어_다음_페이지의_데이터를_조회한다() {
        // given
        memberJpaRepository.save(MemberFixture.createMemberWithOutId("user1", "유저1"));
        memberJpaRepository.save(MemberFixture.createMemberWithOutId("user2", "유저2"));
        var member3 = memberJpaRepository.save(MemberFixture.createMemberWithOutId("user3", "유저3"));
        var member4 = memberJpaRepository.save(MemberFixture.createMemberWithOutId("user4", "유저4"));
        var member5 = memberJpaRepository.save(MemberFixture.createMemberWithOutId("user5", "유저5"));

        // when - 2페이지, size=2 → offset=2, limit=3(+1)
        List<Member> result = memberQueryDslRepository.findMembers("", SortBy.JOIN_ASC, new OffsetLimit(2, 2));

        // then - user3, user4, user5(hasNext 감지용) 조회
        assertThat(result).hasSize(3)
                .extracting(Member::getId)
                .containsExactly(
                        member3.getId(),
                        member4.getId(),
                        member5.getId()
                );
    }

}
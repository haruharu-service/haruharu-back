package org.kwakmunsu.haruhana.admin.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.Test;
import org.kwakmunsu.haruhana.ControllerTestSupport;
import org.kwakmunsu.haruhana.admin.statistics.service.dto.StatisticsResponse;
import org.kwakmunsu.haruhana.security.annotation.TestAdmin;
import org.kwakmunsu.haruhana.security.annotation.TestMember;

class StatisticsControllerTest extends ControllerTestSupport {

    @TestAdmin
    @Test
    void 관리자가_통계를_조회한다() {
        // given
        StatisticsResponse response = StatisticsResponse.of(1024L, 5L, 312L);
        given(statisticsService.getStatistics()).willReturn(response);

        // when & then
        assertThat(mvcTester.get().uri("/v1/admin/statistics"))
                .apply(print())
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.data.totalMemberCount", v -> v.assertThat().isEqualTo(1024))
                .hasPathSatisfying("$.data.todayProblemCount", v -> v.assertThat().isEqualTo(5))
                .hasPathSatisfying("$.data.todayOnTimeSubmissionCount", v -> v.assertThat().isEqualTo(312));
    }

    @TestAdmin
    @Test
    void 통계_데이터가_모두_0이어도_정상_응답한다() {
        // given
        StatisticsResponse response = StatisticsResponse.of(0L, 0L, 0L);
        given(statisticsService.getStatistics()).willReturn(response);

        // when & then
        assertThat(mvcTester.get().uri("/v1/admin/statistics"))
                .apply(print())
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.data.totalMemberCount", v -> v.assertThat().isEqualTo(0))
                .hasPathSatisfying("$.data.todayProblemCount", v -> v.assertThat().isEqualTo(0))
                .hasPathSatisfying("$.data.todayOnTimeSubmissionCount", v -> v.assertThat().isEqualTo(0));
    }

    @TestMember
    @Test
    void 일반_회원은_통계를_조회할_수_없다() {
        // when & then
        assertThat(mvcTester.get().uri("/v1/admin/statistics"))
                .apply(print())
                .hasStatus(403);
    }

    @Test
    void 비인증_사용자는_통계를_조회할_수_없다() {
        // when & then
        assertThat(mvcTester.get().uri("/v1/admin/statistics"))
                .apply(print())
                .hasStatus(403);
    }

}
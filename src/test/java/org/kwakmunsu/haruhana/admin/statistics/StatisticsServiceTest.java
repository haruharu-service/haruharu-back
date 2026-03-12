package org.kwakmunsu.haruhana.admin.statistics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Test;
import org.kwakmunsu.haruhana.UnitTestSupport;
import org.kwakmunsu.haruhana.admin.statistics.service.StatisticsService;
import org.kwakmunsu.haruhana.admin.statistics.service.dto.StatisticsResponse;
import org.kwakmunsu.haruhana.domain.member.service.MemberReader;
import org.kwakmunsu.haruhana.domain.problem.service.ProblemReader;
import org.kwakmunsu.haruhana.domain.submission.service.SubmissionReader;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class StatisticsServiceTest extends UnitTestSupport {

    @InjectMocks
    private StatisticsService statisticsService;

    @Mock
    private MemberReader memberReader;

    @Mock
    private ProblemReader problemReader;

    @Mock
    private SubmissionReader submissionReader;

    @Test
    void 통계_데이터를_정상적으로_반환한다() {
        // given
        given(memberReader.countAll()).willReturn(1024L);
        given(problemReader.countByProblemAtToday()).willReturn(5L);
        given(submissionReader.countTodayOnTimeSubmissions()).willReturn(312L);

        // when
        StatisticsResponse result = statisticsService.getStatistics();

        // then
        assertThat(result.totalMemberCount()).isEqualTo(1024L);
        assertThat(result.todayProblemCount()).isEqualTo(5L);
        assertThat(result.todayOnTimeSubmissionCount()).isEqualTo(312L);
    }

    @Test
    void 모든_카운트가_0이어도_정상_반환된다() {
        // given
        given(memberReader.countAll()).willReturn(0L);
        given(problemReader.countByProblemAtToday()).willReturn(0L);
        given(submissionReader.countTodayOnTimeSubmissions()).willReturn(0L);

        // when
        StatisticsResponse result = statisticsService.getStatistics();

        // then
        assertThat(result.totalMemberCount()).isZero();
        assertThat(result.todayProblemCount()).isZero();
        assertThat(result.todayOnTimeSubmissionCount()).isZero();
    }

    @Test
    void 문제와_제출이_없어도_회원_수는_반환된다() {
        // given
        given(memberReader.countAll()).willReturn(500L);
        given(problemReader.countByProblemAtToday()).willReturn(0L);
        given(submissionReader.countTodayOnTimeSubmissions()).willReturn(0L);

        // when
        StatisticsResponse result = statisticsService.getStatistics();

        // then
        assertThat(result.totalMemberCount()).isEqualTo(500L);
        assertThat(result.todayProblemCount()).isZero();
        assertThat(result.todayOnTimeSubmissionCount()).isZero();
    }

}
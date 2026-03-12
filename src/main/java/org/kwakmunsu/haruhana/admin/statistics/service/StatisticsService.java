package org.kwakmunsu.haruhana.admin.statistics.service;

import lombok.RequiredArgsConstructor;
import org.kwakmunsu.haruhana.admin.statistics.service.dto.StatisticsResponse;
import org.kwakmunsu.haruhana.domain.member.service.MemberReader;
import org.kwakmunsu.haruhana.domain.problem.service.ProblemReader;
import org.kwakmunsu.haruhana.domain.submission.service.SubmissionReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class StatisticsService {

    private final MemberReader memberReader;
    private final ProblemReader problemReader;
    private final SubmissionReader submissionReader;

    /**
     * 총 회원 수, 오늘의 문제 생성 수, 오늘의 문제 제출 수
     */
    @Transactional(readOnly = true)
    public StatisticsResponse getStatistics() {
        long totalMemberCount = memberReader.countAll();
        long todayProblemCount = problemReader.countByProblemAtToday();
        long todaySubmissionCount = submissionReader.countTodayOnTimeSubmissions();

        return StatisticsResponse.of(totalMemberCount, todayProblemCount, todaySubmissionCount);
    }

}
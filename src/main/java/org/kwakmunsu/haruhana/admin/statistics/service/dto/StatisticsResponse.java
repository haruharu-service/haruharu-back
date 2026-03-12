package org.kwakmunsu.haruhana.admin.statistics.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "통계 응답")
public record StatisticsResponse(

        @Schema(description = "총 활성 회원 수", example = "1024")
        long totalMemberCount,

        @Schema(description = "오늘 날짜(problemAt)로 배정된 문제 수", example = "5")
        long todayProblemCount,

        @Schema(description = "오늘 배정된 문제의 제시간 제출 수", example = "312")
        long todayOnTimeSubmissionCount

) {

    public static StatisticsResponse of(long totalMemberCount, long todayProblemCount, long todaySubmissionCount) {
        return new StatisticsResponse(totalMemberCount, todayProblemCount, todaySubmissionCount);
    }

}
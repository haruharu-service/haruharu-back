package org.kwakmunsu.haruhana.admin.statistics.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.kwakmunsu.haruhana.admin.statistics.service.dto.StatisticsResponse;
import org.kwakmunsu.haruhana.global.support.response.ApiResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "Admin -  Statistics", description = "관리자용 통계 관련 API 문서")
public abstract class StatisticsDocsController {

    @Operation(summary = "통계 정보 조회 - 관리자", description = "총 회원 수, 오늘의 문제 생성 수, 오늘의 문제 제출 수를 조회합니다.")
    public abstract ResponseEntity<ApiResponse<StatisticsResponse>> getStatistics();

}
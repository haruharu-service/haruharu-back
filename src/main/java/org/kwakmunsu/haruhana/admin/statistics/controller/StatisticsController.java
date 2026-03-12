package org.kwakmunsu.haruhana.admin.statistics.controller;

import lombok.RequiredArgsConstructor;
import org.kwakmunsu.haruhana.admin.statistics.service.StatisticsService;
import org.kwakmunsu.haruhana.admin.statistics.service.dto.StatisticsResponse;
import org.kwakmunsu.haruhana.global.support.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class StatisticsController extends StatisticsDocsController {

    private final StatisticsService statisticsService;

    @Override
    @GetMapping("/v1/admin/statistics")
    public ResponseEntity<ApiResponse<StatisticsResponse>> getStatistics() {
        StatisticsResponse response = statisticsService.getStatistics();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

}
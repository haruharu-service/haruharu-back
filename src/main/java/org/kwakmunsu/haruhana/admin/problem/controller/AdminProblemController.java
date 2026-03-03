package org.kwakmunsu.haruhana.admin.problem.controller;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.kwakmunsu.haruhana.admin.problem.service.AdminProblemService;
import org.kwakmunsu.haruhana.admin.problem.service.dto.AdminProblemPreviewResponse;
import org.kwakmunsu.haruhana.global.support.OffsetLimit;
import org.kwakmunsu.haruhana.global.support.response.ApiResponse;
import org.kwakmunsu.haruhana.global.support.response.PageResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class AdminProblemController extends AdminProblemDocsController {

    private final AdminProblemService adminProblemService;

    @Override
    @GetMapping("/v1/admin/problems")
    public ResponseEntity<ApiResponse<PageResponse<AdminProblemPreviewResponse>>> findProblems(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        PageResponse<AdminProblemPreviewResponse> response = adminProblemService.findProblems(
                date,
                new OffsetLimit(page, size)
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

}
package org.kwakmunsu.haruhana.domain.problem.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.kwakmunsu.haruhana.domain.problem.repository.ProblemJpaRepository;
import org.kwakmunsu.haruhana.global.entity.EntityStatus;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProblemReader {

    private final ProblemJpaRepository problemJpaRepository;

    /**
     * problemAt(문제 배정 날짜)이 오늘인 문제 수를 반환합니다.
     * 스케줄러가 오늘 날짜로 생성한 문제 수와 동일합니다.
     */
    public long countByProblemAtToday() {
        LocalDate today = LocalDateTime.now().toLocalDate();

        return problemJpaRepository.countByProblemAtAndStatus(today, EntityStatus.ACTIVE);
    }

}
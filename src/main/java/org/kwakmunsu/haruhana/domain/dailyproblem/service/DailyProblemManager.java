package org.kwakmunsu.haruhana.domain.dailyproblem.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kwakmunsu.haruhana.domain.dailyproblem.entity.DailyProblem;
import org.kwakmunsu.haruhana.domain.dailyproblem.repository.DailyProblemJpaRepository;
import org.kwakmunsu.haruhana.domain.member.entity.Member;
import org.kwakmunsu.haruhana.domain.problem.entity.Problem;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DailyProblemManager {

    private final DailyProblemJpaRepository dailyProblemJpaRepository;

    // TODO: 성능 개선 필요 (batch insert 등)
    public void assignDailyProblemToMembers(Problem problem, List<Member> members, LocalDate targetDate) {
        log.info("[DailyProblemManager] 일일 문제 할당 시작 - problemId={}, memberCount={}, targetDate={}",
                problem.getId(), members.size(), targetDate);

        for (Member member : members) {
            dailyProblemJpaRepository.save(DailyProblem.create(
                    member,
                    problem,
                    targetDate
            ));
        }

        log.info("[DailyProblemManager] 일일 문제 할당 완료 - problemId={}, memberCount={}, targetDate={}",
                problem.getId(), members.size(), targetDate);
    }

}
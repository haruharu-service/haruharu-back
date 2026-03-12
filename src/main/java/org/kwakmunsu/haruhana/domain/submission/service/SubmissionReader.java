package org.kwakmunsu.haruhana.domain.submission.service;

import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.kwakmunsu.haruhana.domain.submission.entity.Submission;
import org.kwakmunsu.haruhana.domain.submission.repository.SubmissionJpaRepository;
import org.kwakmunsu.haruhana.global.entity.EntityStatus;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SubmissionReader {

    private final SubmissionJpaRepository submissionJpaRepository;

    public Optional<Submission> findByMemberIdAndDailyProblemId(Long memberId, Long dailyProblemId) {
        return submissionJpaRepository.findByMemberIdAndDailyProblemIdAndStatus(
                memberId,
                dailyProblemId,
                EntityStatus.ACTIVE
        );
    }

    public long countTodayOnTimeSubmissions() {
        return submissionJpaRepository.countByDailyProblem_AssignedAtAndIsOnTimeAndStatus(
                LocalDate.now(),
                true,
                EntityStatus.ACTIVE
        );

    }

}
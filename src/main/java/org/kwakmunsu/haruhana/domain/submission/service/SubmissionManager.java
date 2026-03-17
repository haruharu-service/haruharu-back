package org.kwakmunsu.haruhana.domain.submission.service;

import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kwakmunsu.haruhana.domain.dailyproblem.entity.DailyProblem;
import org.kwakmunsu.haruhana.domain.submission.entity.Submission;
import org.kwakmunsu.haruhana.domain.submission.repository.SubmissionJpaRepository;
import org.kwakmunsu.haruhana.domain.submission.service.dto.response.SubmissionResult;
import org.kwakmunsu.haruhana.global.entity.EntityStatus;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class SubmissionManager {

    private final SubmissionJpaRepository submissionJpaRepository;

    /**
     * 제출 등록 또는 업데이트 <br>
     * - 이미 제출했다면 답변 업데이트 <br>
     * - 제출 기록이 없다면 새로 생성 <br>
     * - 할당 날짜 내 제출: isOnTime = true (스트릭 증가 가능) <br>
     * - 할당 날짜 지난 후 제출: isOnTime = false (스트릭 증가 안됨) <br>
     *
     * @param dailyProblem 오늘의 문제
     * @param userAnswer 사용자가 제출한 답변
     *
     * @return SubmissionResult (제출 정보와 최초 제출 여부)
     */
    public SubmissionResult submit(DailyProblem dailyProblem, String userAnswer) {
        Optional<Submission> existingSubmission = submissionJpaRepository.findByMemberIdAndDailyProblemIdAndStatus(
                dailyProblem.getMember().getId(),
                dailyProblem.getId(),
                EntityStatus.ACTIVE
        );

        if (existingSubmission.isPresent()) {
            Submission submission = existingSubmission.get();
            submission.updateAnswer(userAnswer);
            log.info("[SubmissionManager] 제출 답변 업데이트 - memberId={}, dailyProblemId={}",
                    dailyProblem.getMember().getId(), dailyProblem.getId());

            return new SubmissionResult(submission, false /*isFirstSubmission */);
        } else {
            Submission saved = submissionJpaRepository.save(Submission.create(
                    dailyProblem.getMember(),
                    dailyProblem,
                    userAnswer,
                    LocalDateTime.now()
            ));
            dailyProblem.markAsSolved();
            log.info("[SubmissionManager] 최초 제출 완료 - memberId={}, dailyProblemId={}",
                    dailyProblem.getMember().getId(), dailyProblem.getId());

            return new SubmissionResult(saved, true /*isFirstSubmission */);
        }
    }

}
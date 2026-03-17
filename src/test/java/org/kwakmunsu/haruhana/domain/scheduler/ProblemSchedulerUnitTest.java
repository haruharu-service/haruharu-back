package org.kwakmunsu.haruhana.domain.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.kwakmunsu.haruhana.UnitTestSupport;
import org.kwakmunsu.haruhana.domain.problem.service.ProblemGenerator;
import org.kwakmunsu.haruhana.global.support.notification.ErrorNotificationSender;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class ProblemSchedulerUnitTest extends UnitTestSupport {

    @Mock
    ProblemGenerator problemGenerator;

    @Mock
    ErrorNotificationSender errorNotificationSender;

    @InjectMocks
    ProblemScheduler problemScheduler;

    @Test
    void 매일_자정에_문제_생성_스케줄러가_정상_실행된다() {
        // given
        doNothing().when(problemGenerator).generateProblem(any(LocalDate.class));

        // when
        problemScheduler.generateDailyProblems();

        // then
        verify(problemGenerator, times(1)).generateProblem(any(LocalDate.class));
    }

    @Test
    void 문제_생성_중_예외가_발생해도_스케줄러는_정상_종료된다() {
        // given
        doThrow(new RuntimeException("문제 생성 실패"))
                .when(problemGenerator).generateProblem(any(LocalDate.class));

        // when
        problemScheduler.generateDailyProblems();

        // then
        verify(problemGenerator, times(1)).generateProblem(any(LocalDate.class));
        verify(errorNotificationSender, times(1)).sendErrorNotification(any(String.class), any(Exception.class));
    }

    @Test
    void targetDate는_현재_날짜의_다음_날이다() {
        // given
        LocalDate expectedDate = LocalDate.now().plusDays(1);
        doNothing().when(problemGenerator).generateProblem(expectedDate);

        // when
        problemScheduler.generateDailyProblems();

        // then
        verify(problemGenerator, times(1)).generateProblem(expectedDate);
    }

}
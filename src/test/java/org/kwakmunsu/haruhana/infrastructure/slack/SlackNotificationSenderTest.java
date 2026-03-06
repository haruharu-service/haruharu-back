package org.kwakmunsu.haruhana.infrastructure.slack;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kwakmunsu.haruhana.UnitTestSupport;
import org.slf4j.MDC;
import org.springframework.test.util.ReflectionTestUtils;

class SlackNotificationSenderTest extends UnitTestSupport {

    private SlackNotificationSender slackNotificationSender;

    @BeforeEach
    void setUp() {
        slackNotificationSender = new SlackNotificationSender();
    }

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void webhookUrl이_비어있으면_전송하지_않는다() {
        // given: webhookUrl을 빈 값으로 설정
        ReflectionTestUtils.setField(slackNotificationSender, "webhookUrl", "");

        // when: 예외가 발생하지 않아야 한다
        slackNotificationSender.sendErrorNotification("테스트 메시지", new RuntimeException("에러"));

        // then: 예외 없이 메서드가 종료됨 (전송 시도 없음)
    }

    @Test
    void webhookUrl이_null이면_전송하지_않는다() {
        // given
        ReflectionTestUtils.setField(slackNotificationSender, "webhookUrl", null);

        // when & then
        slackNotificationSender.sendErrorNotification("테스트 메시지", null);
    }

    @Test
    void throwable이_null이어도_정상_동작한다() throws IOException {
        // given
        MDC.put("traceId", "trace-001");
        MDC.put("httpMethod", "GET");
        MDC.put("requestUri", "/api/test");
        MDC.put("clientIp", "127.0.0.1");

        // when: buildPayload를 직접 호출하여 실제 로직 실행
        String payload = slackNotificationSender.buildPayload("메시지", null);

        // then: throwable이 null이면 스택 트레이스 대신 N/A가 payload에 포함되어야 한다
        assertThat(payload).contains("N/A");
    }

    @Test
    void JSON_특수문자가_포함된_메시지도_정상_처리된다() throws IOException {
        // given: JSON 인젝션 취약점 검증 - 따옴표, 백슬래시, 개행 포함
        String dangerousMessage = "에러 \"발생\": \\path\\to\\file\n줄바꿈 포함";
        RuntimeException exception = new RuntimeException("cause: \"quoted\" and \\backslash\\");

        // when: buildPayload를 직접 호출하여 이스케이프 처리 검증
        String payload = slackNotificationSender.buildPayload(dangerousMessage, exception);

        // then: Jackson이 특수문자를 올바르게 이스케이프하여 유효한 JSON이어야 한다
        assertThat(payload)
                .contains("\\\"발생\\\"")    // " → \" 이스케이프
                .contains("\\\\path")        // \ → \\ 이스케이프
                .contains("\\n줄바꿈 포함"); // 개행 → \n 이스케이프
    }

    @Test
    void queryString이_있으면_URL에_포함된다() throws IOException {
        // given
        MDC.put("traceId", "trace-002");
        MDC.put("httpMethod", "GET");
        MDC.put("requestUri", "/api/problems");
        MDC.put("queryString", "page=1&size=10");
        MDC.put("clientIp", "10.0.0.1");

        // when: buildPayload를 직접 호출하여 queryString 결합 로직 검증
        String payload = slackNotificationSender.buildPayload("쿼리스트링 포함 테스트", new RuntimeException("test"));

        // then: payload에 requestUri + "?" + queryString 형태가 포함되어야 한다
        assertThat(payload).contains("/api/problems?page=1&size=10");
    }

    @Test
    void MDC_값이_없으면_NA_기본값으로_처리된다() throws IOException {
        // given: MDC 아무 값도 없음

        // when: buildPayload를 직접 호출하여 MDC 기본값 처리 검증
        String payload = slackNotificationSender.buildPayload("MDC 없는 케이스", new IllegalStateException("상태 오류"));

        // then: traceId, httpMethod, requestUri, clientIp 모두 N/A로 대체되어야 한다
        assertThat(payload).contains("N/A");
    }

}
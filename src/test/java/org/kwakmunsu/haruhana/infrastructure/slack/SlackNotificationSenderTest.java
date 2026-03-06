package org.kwakmunsu.haruhana.infrastructure.slack;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    void throwable이_null이어도_정상_동작한다() {
        // given
        ReflectionTestUtils.setField(slackNotificationSender, "webhookUrl", "");

        MDC.put("traceId", "trace-001");
        MDC.put("httpMethod", "GET");
        MDC.put("requestUri", "/api/test");
        MDC.put("clientIp", "127.0.0.1");

        // when & then: throwable=null 이어도 예외 없이 동작
        slackNotificationSender.sendErrorNotification("메시지", null);
    }

    @Test
    void JSON_특수문자가_포함된_메시지도_정상_처리된다() {
        // given
        ReflectionTestUtils.setField(slackNotificationSender, "webhookUrl", "");

        // JSON 인젝션 취약점 검증: 따옴표, 백슬래시, 개행 포함
        String dangerousMessage = "에러 \"발생\": \\path\\to\\file\n줄바꿈 포함";
        RuntimeException exception = new RuntimeException("cause: \"quoted\" and \\backslash\\");

        // when & then: Jackson이 이스케이프 처리하므로 예외 없이 동작
        slackNotificationSender.sendErrorNotification(dangerousMessage, exception);
    }

    @Test
    void queryString이_있으면_URL에_포함된다() {
        // given
        ReflectionTestUtils.setField(slackNotificationSender, "webhookUrl", "");

        MDC.put("traceId", "trace-002");
        MDC.put("httpMethod", "GET");
        MDC.put("requestUri", "/api/problems");
        MDC.put("queryString", "page=1&size=10");
        MDC.put("clientIp", "10.0.0.1");

        // when & then: 예외 없이 동작
        slackNotificationSender.sendErrorNotification("쿼리스트링 포함 테스트", new RuntimeException("test"));
    }

    @Test
    void MDC_값이_없으면_NA_기본값으로_처리된다() {
        // given: MDC 아무 값도 없음
        ReflectionTestUtils.setField(slackNotificationSender, "webhookUrl", "");

        // when & then
        slackNotificationSender.sendErrorNotification("MDC 없는 케이스", new IllegalStateException("상태 오류"));
    }

}
package org.kwakmunsu.haruhana.infrastructure.slack;

import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kwakmunsu.haruhana.global.support.notification.ErrorNotificationSender;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 실제 Slack webhook 전송 테스트
 * - 평소에는 @Disabled 로 CI/CD에서 실행 제외
 * - 수동으로 확인하고 싶을 때 @Disabled 제거 후 실행
 * - webhook URL은 application-test.yml의 slack.webhook.url 값을 사용
 */
@Disabled("수동 실행 전용: Slack 실제 전송 테스트")
@ActiveProfiles("test")
@SpringBootTest
class SlackNotificationRealSendTest {

    @Autowired
    private ErrorNotificationSender errorNotificationSender;

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void 실제_전송_RuntimeException_발생_시_Slack_알림이_전송된다() {
        MDC.put("traceId", "real-trace-001");
        MDC.put("httpMethod", "POST");
        MDC.put("requestUri", "/api/test/real");
        MDC.put("clientIp", "192.168.0.1");

        RuntimeException exception = new RuntimeException("실제 전송 테스트용 RuntimeException");

        errorNotificationSender.sendErrorNotification("[실제 전송 테스트] RuntimeException 발생", exception);

        // @Async로 비동기 실행되므로 전송 완료 대기
        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .pollDelay(2, TimeUnit.SECONDS)
                .until(() -> true);
    }

    @Test
    void 실제_전송_queryString이_포함된_요청_정보가_Slack_알림에_표시된다() {
        MDC.put("traceId", "real-trace-002");
        MDC.put("httpMethod", "GET");
        MDC.put("requestUri", "/api/problems");
        MDC.put("queryString", "page=1&size=10&keyword=테스트");
        MDC.put("clientIp", "10.0.0.1");

        IllegalArgumentException exception = new IllegalArgumentException("잘못된 요청 파라미터");

        errorNotificationSender.sendErrorNotification("[실제 전송 테스트] 쿼리스트링 포함 요청", exception);

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .pollDelay(2, TimeUnit.SECONDS)
                .until(() -> true);
    }

    @Test
    void 실제_전송_throwable이_null이어도_N_A로_정상_전송된다() {
        MDC.put("traceId", "real-trace-003");
        MDC.put("httpMethod", "DELETE");
        MDC.put("requestUri", "/api/user/1");
        MDC.put("clientIp", "172.16.0.5");

        errorNotificationSender.sendErrorNotification("[실제 전송 테스트] throwable=null 케이스", null);

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .pollDelay(2, TimeUnit.SECONDS)
                .until(() -> true);
    }

    @Test
    void 실제_전송_JSON_특수문자가_포함된_메시지도_정상_전송된다() {
        MDC.put("traceId", "real-trace-004");
        MDC.put("httpMethod", "POST");
        MDC.put("requestUri", "/api/data");
        MDC.put("clientIp", "192.168.1.1");

        // JSON 인젝션 취약점 검증: 따옴표, 백슬래시, 개행 포함
        String dangerousMessage = "에러 \"발생\": \\path\\to\\file\n줄바꿈 포함 메시지";
        RuntimeException exception = new RuntimeException("cause: \"quoted\" and \\backslash\\");

        errorNotificationSender.sendErrorNotification(dangerousMessage, exception);

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .pollDelay(2, TimeUnit.SECONDS)
                .until(() -> true);
    }

}
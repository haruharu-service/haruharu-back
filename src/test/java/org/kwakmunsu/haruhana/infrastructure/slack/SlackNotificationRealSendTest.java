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

@Disabled
@ActiveProfiles("test")
@SpringBootTest
class SlackNotificationRealSendTest{

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
}
package org.kwakmunsu.haruhana.infrastructure.slack;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.kwakmunsu.haruhana.global.support.notification.ErrorNotificationSender;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class SlackNotificationSender implements ErrorNotificationSender {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final int STACK_TRACE_LINES = 5;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static final String NA = "N/A";


    @Value("${slack.webhook.url:}")
    private String webhookUrl;

    @Async
    public void sendErrorNotification(String message, Throwable throwable) {
        if (!StringUtils.hasText(webhookUrl)) {
            return;
        }

        try {
            String payload = buildPayload(message, throwable);
            sendToSlack(payload);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("[SlackNotificationSender] Slack 알림 전송 중단 (인터럽트): {}", ex.getMessage());
        } catch (IOException ex) {
            log.warn("[SlackNotificationSender] Slack 알림 전송 실패: {}", ex.getMessage());
        }
    }

    String buildPayload(String message, Throwable throwable) throws IOException {
        String time = LocalDateTime.now().format(FORMATTER);
        String traceId = mdcOrDefault("traceId");
        String httpMethod = mdcOrDefault("httpMethod");
        String requestUri = mdcOrDefault("requestUri");
        String queryString = MDC.get("queryString");
        String clientIp = mdcOrDefault("clientIp");
        String stackTrace = extractStackTrace(throwable);

        String urlLine = queryString != null && !queryString.isBlank()
                ? requestUri + "?" + queryString
                : requestUri;

        String text = """
                🚨 *[ERROR] 서버 에러 발생*
                • *시간*: %s
                • *traceId*: %s
                • *요청*: `%s %s`
                • *클라이언트 IP*: %s
                • *메세지*: %s
                • *스택 트레이스*:
                ```
                %s
                ```""".formatted(time, traceId, httpMethod, urlLine, clientIp, message, stackTrace);

        // Jackson이 text 값의 특수문자(", \, 개행 등)를 자동으로 이스케이프
        return OBJECT_MAPPER.writeValueAsString(Map.of("text", text));
    }

    private String mdcOrDefault(String key) {
        String value = MDC.get(key);

        return value != null ? value : NA;
    }

    private String extractStackTrace(Throwable throwable) {
        if (throwable == null) {
            return NA;
        }
        return Arrays.stream(throwable.getStackTrace())
                .limit(STACK_TRACE_LINES)
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n"));
    }

    private void sendToSlack(String payload) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.warn("[SlackNotificationSender] Slack webhook 응답 오류: status={}, body={}",
                    response.statusCode(),
                    response.body()
            );
        }
    }

}
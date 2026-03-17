package org.kwakmunsu.haruhana.infrastructure.gemini;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;  // Spring AI 에서 제공하는 ChatClient 빈 주입

    @Retryable(
            retryFor = {RestClientException.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 1000)
    )
    public <T> T sendPrompt(String prompt, Class<T> responseType) {
        log.info("[ChatService] AI API 호출 시작 - responseType={}", responseType.getSimpleName());
        T result = chatClient.prompt()
                .user(prompt)
                .options(ChatOptions.builder()
                        .temperature(0.5)
                        .build())
                .call()
                .entity(responseType);
        log.info("[ChatService] AI API 호출 완료 - responseType={}", responseType.getSimpleName());
        return result;
    }

}
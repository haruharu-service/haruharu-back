package org.kwakmunsu.haruhana.global.support.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * MDC(Mapped Diagnostic Context)에 요청 컨텍스트 정보를 설정하는 필터
 * - traceId: 요청 추적을 위한 ID
 * - httpMethod: HTTP 메서드 (GET, POST 등)
 * - requestUri: 요청 URI
 * - queryString: 쿼리 파라미터
 * - clientIp: 클라이언트 IP
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcFilter extends OncePerRequestFilter {

    private static final String TRACE_ID = "traceId";
    private static final String HTTP_METHOD = "httpMethod";
    private static final String REQUEST_URI = "requestUri";
    private static final String QUERY_STRING = "queryString";
    private static final String CLIENT_IP = "clientIp";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            MDC.put(TRACE_ID, traceId);
            MDC.put(HTTP_METHOD, request.getMethod());
            MDC.put(REQUEST_URI, request.getRequestURI());
            MDC.put(QUERY_STRING, request.getQueryString() != null ? request.getQueryString() : "");
            MDC.put(CLIENT_IP, resolveClientIp(request));

            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            return ip.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

}
package org.kwakmunsu.haruhana.global.security.jwt;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityPaths {

    public static final String[] ACTUATOR_PERMIT = {
            "/actuator/health",
            "/actuator/info",
            "/actuator/prometheus"
    };

    public static final String[] PERMIT_ALL = {
            "/v1/categories",
            "/v1/auth/login",
            "/v1/auth/reissue",
            "/v1/members/sign-up",
            "/v1/members/nickname",
            "/v1/members/login-id",
            "/error",
            "/swagger/**",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };

    public static final String[] ADMIN = {
            "/v1/admin/**"
    };

}
package org.kwakmunsu.haruhana.security;

import lombok.RequiredArgsConstructor;
import org.kwakmunsu.haruhana.global.security.jwt.SecurityPaths;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@RequiredArgsConstructor
@EnableWebSecurity
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SecurityPaths.ACTUATOR_PERMIT).permitAll()
                        .requestMatchers("/actuator/**").denyAll()
                        .requestMatchers(SecurityPaths.PERMIT_ALL).permitAll()
                        .requestMatchers(SecurityPaths.ADMIN).hasRole("ADMIN")
                        .anyRequest().hasAnyRole("MEMBER", "ADMIN")
                );

        return http.build();
    }

}
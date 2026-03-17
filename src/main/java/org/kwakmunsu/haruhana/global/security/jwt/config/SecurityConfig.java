package org.kwakmunsu.haruhana.global.security.jwt.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.kwakmunsu.haruhana.global.security.jwt.JwtAccessDeniedHandler;
import org.kwakmunsu.haruhana.global.security.jwt.JwtAuthenticationEntryPoint;
import org.kwakmunsu.haruhana.global.security.jwt.JwtFilter;
import org.kwakmunsu.haruhana.global.security.jwt.JwtProvider;
import org.kwakmunsu.haruhana.global.security.jwt.SecurityPaths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private static final List<String> ALLOWED_METHODS = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    @Value("${cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http
          .authorizeHttpRequests(auth -> auth
                .requestMatchers(SecurityPaths.ACTUATOR_PERMIT).permitAll()
                .requestMatchers("/actuator/**").denyAll()
                .requestMatchers(SecurityPaths.PERMIT_ALL).permitAll()
                .requestMatchers(SecurityPaths.ADMIN).hasRole("ADMIN")
                .anyRequest().hasAnyRole("MEMBER", "ADMIN")
        );

        http
                .addFilterBefore(new JwtFilter(jwtProvider, objectMapper), UsernamePasswordAuthenticationFilter.class);

        http
                .exceptionHandling(handle -> handle
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler));

        http
                .cors(
                        corsCustomizer -> corsCustomizer.configurationSource(request -> {
                            CorsConfiguration config = new CorsConfiguration();
                            config.setAllowedOrigins(allowedOrigins);
                            config.setAllowedMethods(ALLOWED_METHODS);
                            config.setAllowedHeaders(List.of("*"));
                            config.setAllowCredentials(true);
                            config.setMaxAge(3600L); // 1 hour

                            return config;
                        })
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
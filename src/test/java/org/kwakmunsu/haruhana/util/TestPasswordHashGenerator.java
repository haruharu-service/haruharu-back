package org.kwakmunsu.haruhana.util;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 성능 테스트용 비밀번호 해시 생성
 * 실행: ./gradlew test --tests TestPasswordHashGenerator
 */
public class TestPasswordHashGenerator {

    @Test
    void generateHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = "TestPassword1@";
        String hash = encoder.encode(password);

        System.out.println("========================================");
        System.out.println("비밀번호: " + password);
        System.out.println("BCrypt 해시:");
        System.out.println(hash);
        System.out.println("========================================");

        // 검증
        boolean matches = encoder.matches(password, hash);
        System.out.println("검증 결과: " + (matches ? "✅ 성공" : "❌ 실패"));
    }
}
package org.kwakmunsu.haruhana.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.kwakmunsu.haruhana.domain.member.enums.Role;
import org.kwakmunsu.haruhana.global.entity.BaseEntity;
import org.kwakmunsu.haruhana.global.security.jwt.TokenHasher;
import org.kwakmunsu.haruhana.global.support.error.ErrorType;
import org.kwakmunsu.haruhana.global.support.error.HaruHanaException;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Member extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    private LocalDateTime lastLoginAt;

    private String refreshToken;

    private String profileImageObjectKey; // s3 object key

    public static Member createMember(String loginId, String password, String nickname, Role role) {
        Member member = new Member();

        member.loginId = loginId;
        member.password = password;
        member.nickname = nickname;
        member.role = role;
        member.lastLoginAt = null;
        member.refreshToken = null;
        member.profileImageObjectKey = null;

        return member;
    }

    public void updateProfile(String nickname) {
        this.nickname = nickname;
    }

    public void updateLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public void updateRole(Role role) {
        this.role = Objects.requireNonNull(role);
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = TokenHasher.hash(refreshToken);
    }

    public void clearRefreshToken() {
        this.refreshToken = null;
    }

    public void updateProfileImageObjectKey(String profileImageObjectKey) {
        this.profileImageObjectKey = profileImageObjectKey;
    }

    public boolean hasMatchingObjectKey(String objectKey) {
        return this.profileImageObjectKey != null
                && this.profileImageObjectKey.equals(objectKey);
    }

    public boolean hasMatchingNickname(String nickname) {
        return this.nickname.equals(nickname);
    }

    public void anonymize() {
        String suffix = this.getId() + "_" + UUID.randomUUID().toString().substring(0, 8);
        this.loginId = "deleted_" + suffix;
        this.nickname = "deleted_" + suffix;
    }

    public void validatePassword(PasswordEncoder passwordEncoder, String targetPassword) {
        if (!passwordEncoder.matches(targetPassword, this.password)) {
            throw new HaruHanaException(ErrorType.INVALID_ACCOUNT);
        }
    }

}
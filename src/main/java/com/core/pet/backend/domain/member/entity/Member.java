package com.core.pet.backend.domain.member.entity;

import com.core.pet.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SocialProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    @Builder
    private Member(String name, String email, String password, SocialProvider provider, MemberRole role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.provider = provider;
        this.role = role;
    }

    // 로컬 회원가입
    public static Member ofLocal(String name, String email, String encodedPassword) {
        return Member.builder()
                .name(name)
                .email(email)
                .password(encodedPassword)
                .provider(SocialProvider.LOCAL)
                // 자영업자는 권한을 다르게 줄지 회의
                .role(MemberRole.ROLE_USER)
                .build();
    }

    // OAuth2 소셜 로그인
    public static Member ofOAuth(String name, String email, SocialProvider provider) {
        return Member.builder()
                .name(name)
                .email(email)
                .provider(provider)
                // 소셜로그인 권한은 USER
                .role(MemberRole.ROLE_USER)
                .build();
    }

    public Member updateOAuthInfo(String name) {
        this.name = name;
        return this;
    }
}

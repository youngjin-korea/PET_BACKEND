package com.core.pet.backend.domain.member.dto;

import com.core.pet.backend.domain.member.entity.Member;
import com.core.pet.backend.domain.member.entity.MemberRole;
import com.core.pet.backend.domain.member.entity.SocialProvider;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

public class MemberResponseDto {

    @Getter
    @Builder
    public static class Info {
        private Long id;
        private String name;
        private String email;
        private SocialProvider provider;
        private MemberRole role;
        private LocalDateTime createdAt;

        public static Info from(Member member) {
            return Info.builder()
                    .id(member.getId())
                    .name(member.getName())
                    .email(member.getEmail())
                    .provider(member.getProvider())
                    .role(member.getRole())
                    .createdAt(member.getCreatedAt())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class TokenInfo {
        private String grantType;
        private String accessToken;
        private String refreshToken;
    }
}

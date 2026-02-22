package com.core.pet.backend.domain.member.service;

import com.core.pet.backend.global.exception.CustomException;
import com.core.pet.backend.global.exception.ErrorCode;
import com.core.pet.backend.global.security.jwt.JwtToken;
import com.core.pet.backend.global.security.jwt.JwtTokenProvider;
import com.core.pet.backend.domain.member.entity.Member;
import com.core.pet.backend.domain.member.entity.RefreshToken;
import com.core.pet.backend.domain.member.dto.MemberRequestDto;
import com.core.pet.backend.domain.member.dto.MemberResponseDto;
import com.core.pet.backend.domain.member.repository.MemberRepository;
import com.core.pet.backend.domain.member.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public MemberResponseDto.Info signUp(MemberRequestDto.SignUp request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.MEMBER_ALREADY_EXISTS);
        }
        Member member = Member.ofLocal(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword())
        );
        return MemberResponseDto.Info.from(memberRepository.save(member));
    }

    @Transactional
    public MemberResponseDto.TokenInfo login(MemberRequestDto.Login request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                member.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority(member.getRole().name()))
        );

        JwtToken token = jwtTokenProvider.generateToken(authentication);

        // Refresh Token DB 저장 (이미 있으면 갱신, 없으면 새로 저장)
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(jwtTokenProvider.getRefreshTokenExpiration() / 1000);

        refreshTokenRepository.findByEmail(member.getEmail())
                .ifPresentOrElse(
                        saved -> saved.updateToken(token.getRefreshToken(), expiresAt),
                        () -> refreshTokenRepository.save(
                                RefreshToken.of(member.getEmail(), token.getRefreshToken(), expiresAt)
                        )
                );

        return MemberResponseDto.TokenInfo.builder()
                .grantType(token.getGrantType())
                .accessToken(token.getAccessToken())
                .refreshToken(token.getRefreshToken())
                .build();
    }

    @Transactional
    public MemberResponseDto.TokenInfo reissue(String refreshTokenValue) {
        // 1. Refresh Token 유효성 검사 및 이메일 추출
        String email = jwtTokenProvider.getEmailFromRefreshToken(refreshTokenValue);

        // 2. DB에 저장된 Refresh Token 조회
        RefreshToken savedToken = refreshTokenRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        // 3. 요청으로 들어온 토큰과 DB 토큰 비교 (탈취 방지)
        if (!savedToken.getToken().equals(refreshTokenValue)) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_MISMATCH);
        }

        // 4. 회원 조회 후 새 토큰 발급
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                member.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority(member.getRole().name()))
        );

        JwtToken newToken = jwtTokenProvider.generateToken(authentication);

        // 5. DB의 Refresh Token 갱신 (RTR: Refresh Token Rotation)
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(jwtTokenProvider.getRefreshTokenExpiration() / 1000);
        savedToken.updateToken(newToken.getRefreshToken(), expiresAt);

        return MemberResponseDto.TokenInfo.builder()
                .grantType(newToken.getGrantType())
                .accessToken(newToken.getAccessToken())
                .refreshToken(newToken.getRefreshToken())
                .build();
    }

    public MemberResponseDto.Info getMyInfo(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        return MemberResponseDto.Info.from(member);
    }
}


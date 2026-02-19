package com.core.pet.backend.domain.member.service;

import com.core.pet.backend.global.exception.CustomException;
import com.core.pet.backend.global.exception.ErrorCode;
import com.core.pet.backend.global.security.jwt.JwtToken;
import com.core.pet.backend.global.security.jwt.JwtTokenProvider;
import com.core.pet.backend.domain.member.entity.Member;
import com.core.pet.backend.domain.member.dto.MemberRequestDto;
import com.core.pet.backend.domain.member.dto.MemberResponseDto;
import com.core.pet.backend.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
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

        return MemberResponseDto.TokenInfo.builder()
                .grantType(token.getGrantType())
                .accessToken(token.getAccessToken())
                .refreshToken(token.getRefreshToken())
                .build();
    }

    public MemberResponseDto.Info getMyInfo(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        return MemberResponseDto.Info.from(member);
    }
}

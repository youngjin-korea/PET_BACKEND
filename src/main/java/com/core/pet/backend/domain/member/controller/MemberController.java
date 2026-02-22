package com.core.pet.backend.domain.member.controller;

import com.core.pet.backend.global.response.ApiResponse;
import com.core.pet.backend.domain.member.dto.MemberRequestDto;
import com.core.pet.backend.domain.member.dto.MemberResponseDto;
import com.core.pet.backend.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth / Member", description = "회원 인증 및 정보 API")
@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "회원가입")
    @PostMapping("/api/auth/signup")
    public ResponseEntity<ApiResponse<MemberResponseDto.Info>> signUp(
            @RequestBody @Valid MemberRequestDto.SignUp request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("회원가입이 완료되었습니다.", memberService.signUp(request)));
    }

    @Operation(summary = "로그인")
    @PostMapping("/api/auth/login")
    public ResponseEntity<ApiResponse<MemberResponseDto.TokenInfo>> login(
            @RequestBody @Valid MemberRequestDto.Login request) {
        return ResponseEntity.ok(ApiResponse.ok(memberService.login(request)));
    }

    /**
     * 1. Access Token 만료 시 (401 EXPIRED_TOKEN 응답)
     * 2. POST /api/auth/refresh
     *    Header: Refresh-Token: {refreshToken}
     * 3. 응답으로 새 accessToken + refreshToken 받아서 저장
     * 4. 원래 요청 재시도
     * @param refreshToken
     * @return
     */
    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 Access Token과 Refresh Token을 재발급합니다.")
    @PostMapping("/api/auth/refresh")
    public ResponseEntity<ApiResponse<MemberResponseDto.TokenInfo>> refresh(
            @RequestHeader("Refresh-Token") String refreshToken) {
        return ResponseEntity.ok(ApiResponse.ok("토큰이 재발급되었습니다.", memberService.reissue(refreshToken)));
    }

    @Operation(summary = "내 정보 조회")
    @GetMapping("/api/members/me")
    public ResponseEntity<ApiResponse<MemberResponseDto.Info>> getMyInfo(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(memberService.getMyInfo(userDetails.getUsername())));
    }
}

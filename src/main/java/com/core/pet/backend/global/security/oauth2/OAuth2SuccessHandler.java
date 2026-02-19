package com.core.pet.backend.global.security.oauth2;

import com.core.pet.backend.global.security.jwt.JwtToken;
import com.core.pet.backend.global.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    // 프론트 리다이렉트 URL (실제 환경에 맞게 수정)
    private static final String REDIRECT_URI = "http://localhost:3000/oauth2/callback";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        JwtToken token = jwtTokenProvider.generateToken(authentication);
        log.info("[OAuth2SuccessHandler] accessToken 발급: {}", token.getAccessToken());

        // 토큰을 쿼리 파라미터로 전달하거나 쿠키로 전달
        String targetUrl = REDIRECT_URI
                + "?accessToken=" + token.getAccessToken()
                + "&refreshToken=" + token.getRefreshToken();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}

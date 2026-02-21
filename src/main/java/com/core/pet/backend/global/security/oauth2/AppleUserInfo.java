package com.core.pet.backend.global.security.oauth2;

import java.util.Map;

/**
 * Apple OAuth2 사용자 정보
 * Apple은 OIDC 방식으로 id_token(JWT)을 통해 사용자 정보를 제공합니다.
 * - sub   : Apple 고유 사용자 식별자
 * - email : 사용자 이메일 (최초 1회 또는 비공개 릴레이 이메일)
 * - name  : Apple은 최초 로그인 시에만 name을 제공하므로, 없으면 이메일 앞부분을 사용합니다.
 */
public class AppleUserInfo extends OAuth2UserInfo {

    public AppleUserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getName() {
        // Apple은 최초 로그인 시에만 name 제공 → 없으면 email 앞부분으로 대체
        String name = (String) attributes.get("name");
        if (name != null && !name.isBlank()) return name;
        String email = getEmail();
        return (email != null && email.contains("@")) ? email.split("@")[0] : "AppleUser";
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }
}

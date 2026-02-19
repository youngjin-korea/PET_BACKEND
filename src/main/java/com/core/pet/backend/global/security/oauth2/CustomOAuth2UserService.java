package com.core.pet.backend.global.security.oauth2;

import com.core.pet.backend.domain.member.entity.Member;
import com.core.pet.backend.domain.member.entity.SocialProvider;
import com.core.pet.backend.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        Map<String, Object> attributes = oAuth2User.getAttributes();
        OAuth2UserInfo userInfo = OAuth2UserInfo.of(registrationId, attributes);

        Member member = saveOrUpdate(userInfo, registrationId);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(member.getRole().name())),
                attributes,
                userNameAttributeName
        );
    }

    private Member saveOrUpdate(OAuth2UserInfo userInfo, String registrationId) {
        SocialProvider provider = SocialProvider.valueOf(registrationId.toUpperCase());

        // Email로 조회 있으면 name 수정, 없으면 회원가입 저장
        return memberRepository.findByEmail(userInfo.getEmail())
                .map(member -> member.updateOAuthInfo(userInfo.getName()))
                .orElseGet(() -> memberRepository.save(
                        Member.ofOAuth(userInfo.getName(), userInfo.getEmail(), provider)
                ));
    }
}

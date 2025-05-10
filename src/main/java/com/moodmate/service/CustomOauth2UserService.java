package com.moodmate.service;

import com.moodmate.entity.Member;
import com.moodmate.oauth.CustomOauth2User;
import com.moodmate.oauth.GoogleUserDetails;
import com.moodmate.oauth.OAuth2UserInfo;
import com.moodmate.oauth.Role;
import com.moodmate.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOauth2UserService extends DefaultOAuth2UserService {
    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("getAttributes : {}",oAuth2User.getAttributes());

        String provider = userRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo oAuth2UserInfo = null;

        // 뒤에 진행할 다른 소셜 서비스 로그인을 위해 구분 => 구글
        if(provider.equals("google")){
            log.info("구글 로그인");
            oAuth2UserInfo = new GoogleUserDetails(oAuth2User.getAttributes());

        }

        String providerId = oAuth2UserInfo.getProviderId();
        String loginId = provider + "_" + providerId;
        String email = oAuth2UserInfo.getEmail();
        String picture = oAuth2UserInfo.getPicture();

        Optional<Member> findMember = memberRepository.findByLoginId(loginId);
        Member member;

        if (findMember.isPresent()) {
            member = findMember.get();
        } else {
            // 신규 사용자 → 닉네임 미정 상태로 저장
            member = memberRepository.save(Member.builder()
                    .loginId(loginId)
                    .provider(provider)
                    .providerId(providerId)
                    .role(Role.USER)
                    .pictureUrl(picture)
                    .email(email)
                    .username(null) // 닉네임 미정
                    .build());
        }

        return new CustomOauth2User(member, oAuth2User.getAttributes());
    }
}

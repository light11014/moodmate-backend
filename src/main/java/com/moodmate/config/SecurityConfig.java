package com.moodmate.config;

import com.moodmate.oauth.OAuth2LoginSuccessHandler;
import com.moodmate.oauth.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{

        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/", "/index.html", "/api/auth/login/**", "/login/oauth2/code/**").permitAll()
                        .requestMatchers("/api/auth/login/admin").hasRole(Role.ADMIN.name())
                        .requestMatchers("/api/users/**").hasAnyRole(Role.ADMIN.name(), Role.USER.name())
                        .requestMatchers("/api/auth/logout").authenticated()
                        .anyRequest().authenticated()
                );

        // 소셜 로그인 설정 (경로 커스터마이징)
        http
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(endpoint ->
                                endpoint.baseUri("/api/auth/login") // 로그인 시작 경로 변경
                        )
                        .successHandler(oAuth2LoginSuccessHandler)
                );

        // csrf : 사이트 위변조 방지 설정 (스프링 시큐리티에는 자동으로 설정 되어 있음)
        // csrf기능 켜져있으면 post 요청을 보낼때 csrf 토큰도 보내줘야 로그인 진행됨 !
        // 개발단계에서만 csrf 잠시 꺼두기
        http
                .csrf((auth) -> auth.disable());

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(){

        return new BCryptPasswordEncoder();
    }
}

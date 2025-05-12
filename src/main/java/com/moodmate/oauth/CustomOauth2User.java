package com.moodmate.oauth;

import com.moodmate.entity.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class CustomOauth2User implements UserDetails, OAuth2User {

    @Getter
    private final Member member;
    private Map<String, Object> attributes;

    public CustomOauth2User(Member member, Map<String, Object> attributes) {

        this.member = member;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add((GrantedAuthority) () -> "ROLE_" + member.getRole().name());

        return collection;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return member.getLoginId();
    }

    public String getEmail() {
        return member.getEmail();
    }

    public String getNickname() {
        return member.getUsername();
    }

}
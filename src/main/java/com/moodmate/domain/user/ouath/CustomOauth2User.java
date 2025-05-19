package com.moodmate.domain.user.ouath;

import com.moodmate.domain.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class CustomOauth2User implements UserDetails, OAuth2User {

    @Getter
    private final User user;
    private Map<String, Object> attributes;

    public CustomOauth2User(User user, Map<String, Object> attributes) {

        this.user = user;
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
        collection.add((GrantedAuthority) () -> "ROLE_" + user.getRole().name());

        return collection;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return user.getLoginId();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public String getNickname() {
        return user.getUsername();
    }

}
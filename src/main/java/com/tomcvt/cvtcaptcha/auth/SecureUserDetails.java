package com.tomcvt.cvtcaptcha.auth;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.tomcvt.cvtcaptcha.model.User;

public class SecureUserDetails implements UserDetails {
    private boolean trusted;
    private final User user;
    private String ip;

    public SecureUserDetails(boolean trusted, User user, String ip) {
        this.user = user;
        this.ip = ip;
        this.trusted = trusted;
    }

    public User getUser() {
        return user;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public boolean isTrusted() {
        return trusted;
    }

    public void setTrusted(boolean trusted) {
        this.trusted = trusted;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user == null) {
            return List.of(new SimpleGrantedAuthority("ROLE_ANON"));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
    }

    @Override
    public String getPassword() {
        return user != null ? user.getPassword() : "";
    }
    @Override
    public String getUsername() {
        return user != null ? user.getUsername() : "anonymous";
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override
    public boolean isEnabled() {
        return user != null ? user.isEnabled() : true;
    }

}

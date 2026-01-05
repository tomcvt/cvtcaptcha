package com.tomcvt.cvtcaptcha.auth;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.tomcvt.cvtcaptcha.exceptions.CaptchaLimitExceededException;
import com.tomcvt.cvtcaptcha.model.User;

public class CachedUserDetails implements UserDetails {
    private final String username;
    private final String role;
    private final Long userId;
    private AtomicInteger remainingRequests;
    private String apiKeyVersion;

    public CachedUserDetails(String username, String role, Long userId, Integer remainingRequests, String apiKeyVersion) {
        this.username = username;
        this.role = role;
        this.userId = userId;
        this.remainingRequests = remainingRequests == null ? null : new AtomicInteger(remainingRequests);
        this.apiKeyVersion = apiKeyVersion;
    }
    public Long getUserId() {
        return userId;
    }

    @Override
    public String getUsername() {
        return username;
    }
    @Override
    public String getPassword() {
        throw new UnsupportedOperationException("CachedUserDetails does not support getPassword()");
    }
    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
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
        return true;
    }
    public Integer getRemainingRequests() {
        return remainingRequests == null ? null : remainingRequests.get();
    }

    public String getApiKeyVersion() {
        return apiKeyVersion;
    }

    /**
     * Decrements the count of remaining requests for the current user.
     * <p>
     * If the {@code remainingRequests} counter is {@code null}, the method returns immediately.
     * If the counter is decremented to a value below zero, a {@link CaptchaLimitExceededException}
     * is thrown to indicate that the API key has exceeded its allowed request limit.
     *
     * @throws CaptchaLimitExceededException if no remaining requests are available for this API key
     */
    public void useRequest() {
        if (remainingRequests == null) {
            return;
        }
        if (remainingRequests.decrementAndGet() >= 0) {
            return;
        } else {
            throw new CaptchaLimitExceededException("No remaining requests available for this API key");
        }
    }

    public void setRemainingRequests(Integer remainingRequests) {
        this.remainingRequests = remainingRequests == null ? null : new AtomicInteger(remainingRequests);
    }

    /**
     * Creates a {@link CachedUserDetails} instance from the provided {@link UserDetails} object,
     * setting the username, the first authority (or "ROLE_ANON" if none), and the specified
     * remaining requests and API key version. The password field is set to {@code null}.
     *
     * @param userDetails the {@link UserDetails} object to extract information from
     * @param remainingRequests the number of remaining requests allowed for the user
     * @param apiKeyVersion the version of the API key associated with the user
     * @return a new {@link CachedUserDetails} instance populated with the provided information
     * @deprecated This method is deprecated and may be removed in future versions.
     */
    @Deprecated
    public static CachedUserDetails fromUserDetails(UserDetails userDetails, Integer remainingRequests, String apiKeyVersion) {
        return new CachedUserDetails(userDetails.getUsername(),
                userDetails.getAuthorities().stream().findFirst().map(GrantedAuthority::getAuthority).orElse("ROLE_ANON"),
                null,
                remainingRequests,
                apiKeyVersion);
    }


    /**
     * Creates a {@link CachedUserDetails} instance from a given {@link User} object,
     * along with the specified remaining requests and API key version.
     *
     * @param user the {@link User} object containing user information
     * @param remainingRequests the number of remaining requests allowed for the user
     * @param apiKeyVersion the version of the API key associated with the user
     * @return a new {@link CachedUserDetails} instance populated with the provided data
     */
    public static CachedUserDetails fromUser(User user, Integer remainingRequests, String apiKeyVersion) {
        return new CachedUserDetails(user.getUsername(),
                "ROLE_" + user.getRole(),
                user.getId(),
                remainingRequests,
                apiKeyVersion);
    }

    public static CachedUserDetails withRemainingRequests(CachedUserDetails original, Integer remainingRequests) {
        return new CachedUserDetails(
            original.getUsername(),
            original.getAuthorities().iterator().next().getAuthority(),
            original.getUserId(),
            remainingRequests,
            original.getApiKeyVersion()
        );
    }
}

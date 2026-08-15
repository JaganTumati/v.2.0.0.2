package com.restaurant.ai.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class AppUserPrincipal implements UserDetails {

    private final UUID userId;
    private final UUID restaurantId;
    private final String email;
    private final String passwordHash;
    private final String roleName;
    private final boolean active;

    public AppUserPrincipal(UUID userId, UUID restaurantId, String email, String passwordHash, String roleName, boolean active) {
        this.userId = userId;
        this.restaurantId = restaurantId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.roleName = roleName;
        this.active = active;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getRestaurantId() {
        return restaurantId;
    }

    public String getRoleName() {
        return roleName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // ROLE_ prefix is Spring Security convention, so hasRole("MANAGER") etc. work in @PreAuthorize.
        return List.of(new SimpleGrantedAuthority("ROLE_" + roleName));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
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
        return active;
    }
}

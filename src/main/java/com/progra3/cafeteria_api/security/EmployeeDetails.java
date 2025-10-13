package com.progra3.cafeteria_api.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
@AllArgsConstructor
public class EmployeeDetails implements UserDetails {

    private final String username;
    private final String password;
    private final Long id;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Long businessId;

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}

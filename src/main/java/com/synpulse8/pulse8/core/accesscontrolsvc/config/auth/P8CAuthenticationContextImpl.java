package com.synpulse8.pulse8.core.accesscontrolsvc.config.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class P8CAuthenticationContextImpl extends P8CAuthenticationContext {

    public static User getSpringUser() {
        return (User) getUser();
    }

    @Override
    public String getUsername() {
        return getSpringUser().getUsername();
    }

    @Override
    public Set<GrantedAuthority> getAuthorities() {
        if (getUser() instanceof User) {
            return (Set<GrantedAuthority>) getSpringUser().getAuthorities();
        }
        return new HashSet<>();
    }

    public List<String> getRoles() {
        return new ArrayList<>();
    }

    @Override
    public List<String> getUserHierarchies() {
        return new ArrayList<>();
    }

    @Override
    public List<String> getUserGroups() {
        return new ArrayList<>();
    }
}

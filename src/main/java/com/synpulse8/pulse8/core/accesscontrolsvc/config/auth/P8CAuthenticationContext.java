package com.synpulse8.pulse8.core.accesscontrolsvc.config.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Set;

public abstract class P8CAuthenticationContext {


    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static String getAuthenticatedName() {
        return getAuthentication().getName();
    }

    public static Object getUser() {
        return getAuthentication().getPrincipal();
    }

    public abstract Set<GrantedAuthority> getAuthorities();

    public abstract String getUsername();

    public abstract List<String> getRoles();

    /**
     * Get user group hierarchy paths delimited by /
     * @return user group hierarchies
     */
    public abstract List<String> getUserHierarchies();

    /**
     * Get user group names
     * @return user group names
     */
    public abstract List<String> getUserGroups();

    public boolean isAdmin() {
        return getRoles().contains(P8CRole.ADMIN);
    }

}

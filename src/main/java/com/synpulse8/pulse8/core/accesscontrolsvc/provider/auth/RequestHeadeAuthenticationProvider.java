package com.synpulse8.pulse8.core.accesscontrolsvc.provider.auth;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.ArrayList;

public class RequestHeadeAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String userid = String.valueOf(authentication.getPrincipal());

        if(StringUtils.isBlank(userid)) throw new BadCredentialsException("No Authentication");

        return new PreAuthenticatedAuthenticationToken(authentication.getPrincipal(), null, new ArrayList<>());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(PreAuthenticatedAuthenticationToken.class);
    }

}

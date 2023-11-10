package com.synpulse8.pulse8.core.yournamebackend.config.auth;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class P8CAuthenticationAttributesImpl implements P8CAuthenticationAttributes {

    public Map<String, Object> getAttributes() {
        if (P8CAuthenticationContext.getUser() instanceof DefaultOidcUser) {
            return P8CAuthenticationContextImpl.getOidcUser().getAttributes();
        }
        return null;
    }

    public String getCurrentClient() {
        return getAttribute(P8CAuthAttribute.CURRENT_CLIENT);
    }

    public String getUsername() {
        return getAttribute(P8CAuthAttribute.USERNAME);
    }

    public String getFullName() {
        return getAttribute(P8CAuthAttribute.FULL_NAME);
    }

    public String getFirstName() {
        return getAttribute(P8CAuthAttribute.FIRST_NAME);
    }

    public String getLastName() {
        return getAttribute(P8CAuthAttribute.LAST_NAME);
    }

    public String getEmail() {
        return getAttribute(P8CAuthAttribute.EMAIL);
    }

    public boolean isEmailVerified() {
        return BooleanUtils.isTrue(getAttribute(P8CAuthAttribute.EMAIL_VERIFIED));
    }

}

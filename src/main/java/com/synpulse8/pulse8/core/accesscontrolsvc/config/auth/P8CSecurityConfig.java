package com.synpulse8.pulse8.core.accesscontrolsvc.config.auth;

import com.synpulse8.pulse8.core.accesscontrolsvc.provider.auth.RequestHeadeAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;
import org.springframework.security.web.header.HeaderWriterFilter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Collections;

@Configuration
@EnableWebSecurity
public class P8CSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http.authorizeHttpRequests((auth) -> auth
                        .anyRequest().authenticated()
                ).addFilter(requestHeaderAuthenticationFilter())
                .build();
    }

    @Bean
    public RequestHeaderAuthenticationFilter requestHeaderAuthenticationFilter() {
        RequestHeaderAuthenticationFilter filter = new RequestHeaderAuthenticationFilter();
        filter.setPrincipalRequestHeader("X-Consumer-Custom-ID");
        filter.setExceptionIfHeaderMissing(false);
        filter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/**"));
        filter.setAuthenticationManager(authenticationManager());
        return filter;
    }

    @Bean
    protected AuthenticationManager authenticationManager() {
        return new ProviderManager((Collections.singletonList(authenticationProvider())));
    }
    @Bean
    protected AuthenticationProvider authenticationProvider() {
        return new RequestHeadeAuthenticationProvider();
    }
}

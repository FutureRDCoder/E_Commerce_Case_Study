package com.ecommerce.security;

import com.ecommerce.model.User;
import com.ecommerce.service.UserIdentityService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KeycloakJwtAuthenticationConverter
        implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserIdentityService userIdentityService;

    public KeycloakJwtAuthenticationConverter(
            UserIdentityService userIdentityService) {
        this.userIdentityService = userIdentityService;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        User user = userIdentityService.resolveOrProvisionUserFromJwt(jwt);
        return new UsernamePasswordAuthenticationToken(
                user,
                "N/A",
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_" + user.getRole().name()
                        )
                )
        );
    }
}

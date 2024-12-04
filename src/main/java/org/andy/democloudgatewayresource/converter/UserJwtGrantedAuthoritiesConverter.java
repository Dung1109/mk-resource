package org.andy.democloudgatewayresource.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
public class UserJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public <U> Converter<Jwt, U> andThen(Converter<? super Collection<GrantedAuthority>, ? extends U> after) {
        return Converter.super.andThen(after);
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt source) {
        var roles = source.getClaimAsStringList("roles");
        log.info("roles: {}", roles);

        // If roles are not present in the JWT token, then use the scopes as roles
        if (roles == null) {
            return source.getClaimAsStringList("scope")
                    .stream()
                    .map(scope -> "SCOPE_" + scope)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }

        // If roles are present in the JWT token, then use the roles as roles
        return roles.stream()
                .map(role -> "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

}

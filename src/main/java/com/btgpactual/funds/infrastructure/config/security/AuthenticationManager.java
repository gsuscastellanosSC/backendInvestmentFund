package com.btgpactual.funds.infrastructure.config.security;

import com.btgpactual.funds.infrastructure.security.JwtService;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.util.Collections;

@Component
public class AuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtService jwtService;

    public AuthenticationManager(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = authentication.getCredentials().toString();
        
        try {
            String username = jwtService.getUsername(token);
            if (username != null && jwtService.validate(token)) {
                return Mono.just(new UsernamePasswordAuthenticationToken(
                        username, 
                        null, 
                        Collections.emptyList()
                ));
            }
        } catch (Exception e) {
            return Mono.empty();
        }
        
        return Mono.empty();
    }
}

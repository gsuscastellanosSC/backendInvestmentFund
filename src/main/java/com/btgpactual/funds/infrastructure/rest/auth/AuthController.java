package com.btgpactual.funds.infrastructure.rest.auth;

import com.btgpactual.funds.infrastructure.security.JwtService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtService jwtService;

    public AuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public Mono<Map<String, String>> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        // Simulación de validación de credenciales
        if ("admin".equals(username) && "btg2026".equals(password)) {
            return Mono.just(Map.of("token", jwtService.generateToken(username)));
        }
        return Mono.error(new RuntimeException("Unauthorized"));
    }
}

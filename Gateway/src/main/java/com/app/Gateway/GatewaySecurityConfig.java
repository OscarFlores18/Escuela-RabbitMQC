package com.app.Gateway;

import javax.crypto.spec.SecretKeySpec;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * El Gateway es el unico punto de entrada: valida el JWT (firma HS256 con
 * jwt.secret) y, una vez autenticado, reenvia el rol al microservicio destino
 * mediante el header interno X-Gateway-Role. Asi los microservicios NO
 * necesitan el JWT_SECRET para autenticar: confian en el Gateway.
 */
@Configuration
@EnableWebSecurity
public class GatewaySecurityConfig {

    @Value("${jwt.secret}")
    private String secretKey;

    @Bean
    public JwtDecoder jwtDecoder() {

        SecretKeySpec key = new SecretKeySpec(
                secretKey.getBytes(),
                "HmacSHA256"
        );

        return NimbusJwtDecoder
                .withSecretKey(key)
                .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            String role = jwt.getClaimAsString("role");

            if (role == null) {
                return List.of();
            }

            return List.of(new SimpleGrantedAuthority("ROLE_" + role));
        });

        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth

                // Preflight CORS
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Login y documentacion (Swagger UI centralizado) publicos.
                .requestMatchers(
                    "/auth/login",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/webjars/**",
                    "/alumno/**",
                    "/curso/**",
                    "/nota/**",
                    "/administracion/**",
                    "/actuator/**"
                ).permitAll()

                // El resto (alumnos, cursos, notas, personal, usuarios) exige JWT.
                .anyRequest().authenticated()
            )

            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )

            // Una vez validado el JWT, propagamos el rol al microservicio.
            .addFilterAfter(
                new GatewayIdentityFilter(),
                BearerTokenAuthenticationFilter.class
            );

        return http.build();
    }
}

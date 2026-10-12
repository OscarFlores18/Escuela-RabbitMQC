package com.app.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;

import jakarta.servlet.http.HttpServletResponse;

/**
 * El microservicio NO valida el JWT: confia en el Gateway. Verificamos que la
 * peticion venga del Gateway (X-Gateway-Secret) y levantamos la identidad a
 * partir del rol que el Gateway propaga (X-Gateway-Role). La autorizacion fina
 * por operacion se define con @PreAuthorize.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${gateway.internal-secret}")
    private String gatewaySecret;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .addFilterAfter(
                    new GatewaySecretFilter(gatewaySecret),
                    SecurityContextHolderFilter.class
            )

            .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                ).permitAll()

                // La autorizacion fina por operacion se define con @PreAuthorize
                .anyRequest().authenticated()
            )

            .exceptionHandling(handling -> handling
                .authenticationEntryPoint((request, response, ex) ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED)));

        return http.build();
    }
}

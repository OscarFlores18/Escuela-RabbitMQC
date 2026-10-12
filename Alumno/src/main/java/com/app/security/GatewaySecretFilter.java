package com.app.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class GatewaySecretFilter extends OncePerRequestFilter {

    private static final String SECRET_HEADER = "X-Gateway-Secret";
    private static final String ROLE_HEADER = "X-Gateway-Role";

    private final String gatewaySecret;

    public GatewaySecretFilter(String gatewaySecret) {
        this.gatewaySecret = gatewaySecret;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String recibido = request.getHeader(SECRET_HEADER);

        if (!esValido(recibido)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    "{\"error\":\"Peticion directa al microservicio denegada. Debe tramitarse a traves del Gateway.\"}"
            );
            return;
        }

        // El Gateway ya valido el JWT: levantamos la identidad desde el rol
        // que propaga en X-Gateway-Role.
        String rol = request.getHeader(ROLE_HEADER);

        if (StringUtils.hasText(rol)) {
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            "gateway",
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + rol))
                    );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private boolean esValido(String recibido) {
        if (!StringUtils.hasText(gatewaySecret) || !StringUtils.hasText(recibido)) {
            return false;
        }
        return MessageDigest.isEqual(
                gatewaySecret.getBytes(StandardCharsets.UTF_8),
                recibido.getBytes(StandardCharsets.UTF_8)
        );
    }
}

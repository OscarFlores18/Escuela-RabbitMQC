package com.app.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Exige que la peticion venga del Gateway (header interno X-Gateway-Secret).
 * Si alguien entra directo al microservicio (por ejemplo a su Swagger en
 * http://localhost:8084/swagger-ui/index.html) no trae el secreto y recibe un
 * 403 con un mensaje que le indica usar el Gateway.
 */
public class GatewayRequiredFilter extends OncePerRequestFilter {

    private static final String SECRET_HEADER = "X-Gateway-Secret";

    private final String secreto;

    public GatewayRequiredFilter(String secreto) {
        this.secreto = secreto;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String recibido = request.getHeader(SECRET_HEADER);

        if (StringUtils.hasText(secreto) && secreto.equals(recibido)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(
                "{\"status\":403,"
                + "\"error\":\"Forbidden\","
                + "\"mensaje\":\"Acceso directo no permitido: este microservicio "
                + "solo responde a traves del Gateway. Usa http://localhost:8081 "
                + "(Swagger centralizado en "
                + "http://localhost:8081/swagger-ui/index.html).\"}"
        );
    }
}

package com.app.Gateway;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Marca toda peticion que sale del Gateway con el header interno
 * X-Gateway-Secret, y (si el JWT fue autenticado) propaga el rol en
 * X-Gateway-Role. Los microservicios exigen ese secreto: si alguien intenta
 * entrar directo a un microservicio (por ejemplo a su Swagger en el puerto
 * 8083/8084/8085/8086), no lo tiene y recibe un 403.
 *
 * Tambien elimina los headers internos que el cliente pudiera enviar, para que
 * no puedan falsificarse desde afuera.
 */
public class GatewayIdentityFilter extends OncePerRequestFilter {

    private static final String ROLE_HEADER = "X-Gateway-Role";
    private static final String SECRET_HEADER = "X-Gateway-Secret";

    private static final Set<String> HEADERS_INTERNOS = Set.of(
            ROLE_HEADER.toLowerCase(),
            SECRET_HEADER.toLowerCase()
    );

    private final String secreto;

    public GatewayIdentityFilter(String secreto) {
        this.secreto = secreto;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String rol = null;

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            rol = jwt.getClaimAsString("role");
        }

        Map<String, String> cabeceras = new LinkedHashMap<>();
        cabeceras.put(SECRET_HEADER, secreto);

        if (rol != null) {
            cabeceras.put(ROLE_HEADER, rol);
        }

        HttpServletRequest requestMutado =
                new HeaderMutatingRequest(request, cabeceras);

        filterChain.doFilter(requestMutado, response);
    }

    private static final class HeaderMutatingRequest extends HttpServletRequestWrapper {

        private final Map<String, String> cabeceras;

        HeaderMutatingRequest(
                HttpServletRequest request,
                Map<String, String> cabeceras) {
            super(request);
            this.cabeceras = cabeceras;
        }

        private String valorDe(String nombre) {
            if (nombre == null) {
                return null;
            }
            for (Map.Entry<String, String> cabecera : cabeceras.entrySet()) {
                if (cabecera.getKey().equalsIgnoreCase(nombre)) {
                    return cabecera.getValue();
                }
            }
            return null;
        }

        private boolean esHeaderPropio(String nombre) {
            if (nombre == null) {
                return false;
            }
            for (String propio : cabeceras.keySet()) {
                if (propio.equalsIgnoreCase(nombre)) {
                    return true;
                }
            }
            return false;
        }

        private boolean esHeaderInterno(String nombre) {
            return nombre != null && HEADERS_INTERNOS.contains(nombre.toLowerCase());
        }

        @Override
        public String getHeader(String name) {
            if (esHeaderPropio(name)) {
                return valorDe(name);
            }
            if (esHeaderInterno(name)) {
                return null;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (esHeaderPropio(name)) {
                String valor = valorDe(name);
                return valor == null
                        ? Collections.emptyEnumeration()
                        : Collections.enumeration(List.of(valor));
            }
            if (esHeaderInterno(name)) {
                return Collections.emptyEnumeration();
            }
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Set<String> nombres = new LinkedHashSet<>();
            Enumeration<String> originales = super.getHeaderNames();

            if (originales != null) {
                while (originales.hasMoreElements()) {
                    String nombre = originales.nextElement();
                    if (esHeaderInterno(nombre) || esHeaderPropio(nombre)) {
                        continue;
                    }
                    nombres.add(nombre);
                }
            }

            for (Map.Entry<String, String> cabecera : cabeceras.entrySet()) {
                if (cabecera.getValue() != null) {
                    nombres.add(cabecera.getKey());
                }
            }

            return Collections.enumeration(nombres);
        }
    }
}

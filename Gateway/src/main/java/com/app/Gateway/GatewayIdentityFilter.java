package com.app.Gateway;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
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
 * Toma el rol del JWT ya autenticado y lo propaga al microservicio destino
 * mediante el header X-Gateway-Role. Tambien elimina los headers internos que
 * el cliente pudiera enviar, para que no puedan falsificarse.
 */
public class GatewayIdentityFilter extends OncePerRequestFilter {

    private static final String ROLE_HEADER = "X-Gateway-Role";
    private static final String SECRET_HEADER = "X-Gateway-Secret";

    private static final Set<String> HEADERS_INTERNOS = Set.of(
            ROLE_HEADER.toLowerCase(),
            SECRET_HEADER.toLowerCase()
    );

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

        HttpServletRequest requestMutado =
                new HeaderMutatingRequest(request, ROLE_HEADER, rol);

        filterChain.doFilter(requestMutado, response);
    }

    private static final class HeaderMutatingRequest extends HttpServletRequestWrapper {

        private final String headerNombre;
        private final String headerValor;

        HeaderMutatingRequest(
                HttpServletRequest request,
                String headerNombre,
                String headerValor) {
            super(request);
            this.headerNombre = headerNombre;
            this.headerValor = headerValor;
        }

        private boolean esHeaderPropio(String nombre) {
            return nombre != null && nombre.equalsIgnoreCase(headerNombre);
        }

        private boolean esHeaderInterno(String nombre) {
            return nombre != null && HEADERS_INTERNOS.contains(nombre.toLowerCase());
        }

        @Override
        public String getHeader(String name) {
            if (esHeaderPropio(name)) {
                return headerValor;
            }
            if (esHeaderInterno(name)) {
                return null;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (esHeaderPropio(name)) {
                return headerValor == null
                        ? Collections.emptyEnumeration()
                        : Collections.enumeration(List.of(headerValor));
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

            if (headerValor != null) {
                nombres.add(headerNombre);
            }

            return Collections.enumeration(nombres);
        }
    }
}

package com.app.Gateway;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Chrome/Edge 142+ aplican Local Network Access (ex Private Network Access):
 * una pagina servida en localhost que llama a localhost se considera una
 * "local network request" y el navegador exige que el preflight (OPTIONS)
 * responda "Access-Control-Allow-Private-Network: true". Sin ese header el
 * navegador bloquea la peticion con un error parecido a CORS, aunque el CORS
 * normal este bien configurado (por eso curl funciona y el navegador no).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LocalNetworkAccessFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            res.setHeader("Access-Control-Allow-Private-Network", "true");
        }

        chain.doFilter(request, response);
    }
}

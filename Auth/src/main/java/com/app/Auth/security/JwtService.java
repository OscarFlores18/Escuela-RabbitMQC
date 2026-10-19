package com.app.Auth.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.app.Auth.entity.Usuario;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;

    @Value("${jwt.expiration}")
    private long expiration;

    public JwtService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String generarToken(Usuario usuario) {

        Instant ahora = Instant.now();

        JwtClaimsSet.Builder builder = JwtClaimsSet.builder()
                .issuer("ms-auth")
                .subject(usuario.getUsername())
                .issuedAt(ahora)
                .expiresAt(ahora.plus(expiration, ChronoUnit.SECONDS))
                .claim("role", usuario.getRol().name());

        // Solo agregamos el vinculo con personal si la cuenta lo tiene asignado.
        if (usuario.getPersonalId() != null) {
            builder.claim("personalId", usuario.getPersonalId());
        }

        JwtClaimsSet claims = builder.build();

        return jwtEncoder
                .encode(JwtEncoderParameters.from(claims))
                .getTokenValue();
    }
}
package com.app.Auth.config;

import com.app.Auth.entity.Rol;
import com.app.Auth.entity.Usuario;
import com.app.Auth.repository.UsuarioRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initUsuarios(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            if (usuarioRepository.count() == 0) {

                usuarioRepository.save(
                        new Usuario(
                                "root",
                                passwordEncoder.encode("1234"),
                                Rol.ROOT
                        )
                );

                usuarioRepository.save(
                        new Usuario(
                                "admin",
                                passwordEncoder.encode("1234"),
                                Rol.ADMIN
                        )
                );

                usuarioRepository.save(
                        new Usuario(
                                "director",
                                passwordEncoder.encode("1234"),
                                Rol.DIRECTOR
                        )
                );

                usuarioRepository.save(
                        new Usuario(
                                "secretaria",
                                passwordEncoder.encode("1234"),
                                Rol.SECRETARIA
                        )
                );

                usuarioRepository.save(
                        new Usuario(
                                "preceptor",
                                passwordEncoder.encode("1234"),
                                Rol.PRECEPTOR
                        )
                );

                usuarioRepository.save(
                        new Usuario(
                                "docente",
                                passwordEncoder.encode("1234"),
                                Rol.DOCENTE
                        )
                );

                usuarioRepository.save(
                        new Usuario(
                                "administrativo",
                                passwordEncoder.encode("1234"),
                                Rol.ADMINISTRATIVO
                        )
                );
            }
        };
    }
}

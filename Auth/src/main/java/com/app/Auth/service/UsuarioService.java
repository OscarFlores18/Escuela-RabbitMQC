package com.app.Auth.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.Auth.dto.UsuarioAltaDTO;
import com.app.Auth.dto.UsuarioModificarDTO;
import com.app.Auth.entity.Usuario;
import com.app.Auth.exception.UsuarioYaExisteException;
import com.app.Auth.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> obtenerPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Usuario crearUsuario(UsuarioAltaDTO dto) {

        usuarioRepository.findByUsername(dto.username()).ifPresent(existente -> {
            throw new UsuarioYaExisteException(
                    "Ya existe un usuario con el nombre: " + dto.username());
        });

        // Una persona del colegio no puede tener mas de una cuenta de acceso.
        if (dto.personalId() != null
                && usuarioRepository.existsByPersonalId(dto.personalId())) {
            throw new UsuarioYaExisteException(
                    "La persona seleccionada ya tiene una cuenta asociada.");
        }

        Usuario usuario = new Usuario(
                dto.username(),
                passwordEncoder.encode(dto.password()),
                dto.rol(),
                dto.personalId());

        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> modificarUsuario(Long id, UsuarioModificarDTO dto) {

        return usuarioRepository.findById(id).map(usuario -> {

            usuarioRepository.findByUsername(dto.username())
                    .filter(otro -> !otro.getId().equals(id))
                    .ifPresent(otro -> {
                        throw new UsuarioYaExisteException(
                                "Ya existe un usuario con el nombre: " + dto.username());
                    });

            usuario.setUsername(dto.username());
            usuario.setRol(dto.rol());

            if (dto.password() != null && !dto.password().isBlank()) {
                usuario.setPassword(passwordEncoder.encode(dto.password()));
            }

            return usuarioRepository.save(usuario);
        });
    }

    public boolean eliminarUsuario(Long id) {

        if (!usuarioRepository.existsById(id)) {
            return false;
        }

        usuarioRepository.deleteById(id);
        return true;
    }
}

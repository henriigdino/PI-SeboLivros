
package com.sebo.livros.service;

import com.sebo.livros.entity.Usuario;
import com.sebo.livros.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuário não encontrado"));

        String tipo = usuario.getTipo() == null ? "FUNCIONARIO"
                : usuario.getTipo().trim().toUpperCase(Locale.ROOT);
        if (!tipo.equals("ADMIN") && !tipo.equals("FUNCIONARIO")) {
            tipo = "FUNCIONARIO";
        }

        return User.withUsername(usuario.getEmail().toLowerCase(Locale.ROOT))
                .password(usuario.getSenha())
                .roles(tipo)
                .disabled(!Boolean.TRUE.equals(usuario.getAtivo()))
                .build();
    }
}

package com.sebo.livros.controller;

import com.sebo.livros.dto.LoginRespostaDTO;
import com.sebo.livros.entity.Usuario;
import com.sebo.livros.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Permite ao front validar se a sessao salva no navegador ainda esta ativa. */
@RestController
public class MeController {

    private final UsuarioRepository usuarioRepository;

    public MeController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/api/me")
    public LoginRespostaDTO me(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessao invalida");
        }
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessao invalida"));
        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario inativo");
        }
        return new LoginRespostaDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getTipo());
    }
}

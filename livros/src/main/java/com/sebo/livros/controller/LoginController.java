package com.sebo.livros.controller;

import com.sebo.livros.dto.LoginDTO;
import com.sebo.livros.dto.LoginRespostaDTO;
import com.sebo.livros.entity.Usuario;
import com.sebo.livros.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequestMapping("/api/login")
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;

    public LoginController(AuthenticationManager authenticationManager,
                           UsuarioRepository usuarioRepository) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO dto,
                                   HttpServletRequest request) {
        String email = dto.email().trim().toLowerCase(Locale.ROOT);
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, dto.senha()));

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            request.getSession(true).setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

            Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElseThrow();
            LoginRespostaDTO resposta = new LoginRespostaDTO(
                    usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getTipo());
            return ResponseEntity.ok(resposta);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("E-mail ou senha inválidos.");
        }
    }
}

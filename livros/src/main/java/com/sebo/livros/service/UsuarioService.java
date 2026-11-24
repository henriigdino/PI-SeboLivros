package com.sebo.livros.service;

import com.sebo.livros.entity.Usuario;
import com.sebo.livros.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
public class UsuarioService {

    private static final Set<String> PERFIS = Set.of("ADMIN", "FUNCIONARIO");

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    @Transactional
    public Usuario salvar(Usuario usuario) {
        if (usuario.getSenha() == null || usuario.getSenha().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha obrigatoria");
        }
        normalizar(usuario);
        validarEmailDuplicado(usuario);
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario atualizar(Usuario usuario, String novaSenha) {
        normalizar(usuario);
        validarEmailDuplicado(usuario);
        if (novaSenha != null && !novaSenha.isBlank()) {
            if (novaSenha.length() < 6) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Senha deve ter entre 6 e 100 caracteres");
            }
            usuario.setSenha(passwordEncoder.encode(novaSenha));
        }
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void excluir(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado"));
        if ("ADMIN".equals(usuario.getTipo())
                && usuarioRepository.countByTipoAndAtivoTrue("ADMIN") <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Nao e possivel excluir o ultimo administrador do sistema");
        }
        usuarioRepository.delete(usuario);
    }

    private void normalizar(Usuario usuario) {
        String nome = texto(usuario.getNome());
        String email = texto(usuario.getEmail());
        String tipo = texto(usuario.getTipo());
        if (nome == null || email == null || tipo == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome, e-mail e perfil sao obrigatorios");
        }
        String perfil = tipo.toUpperCase(Locale.ROOT);
        if (!PERFIS.contains(perfil)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Perfil invalido (ADMIN ou FUNCIONARIO)");
        }
        usuario.setNome(nome);
        usuario.setEmail(email.toLowerCase(Locale.ROOT));
        usuario.setTipo(perfil);
        if (usuario.getAtivo() == null) {
            usuario.setAtivo(true);
        }
    }

    private void validarEmailDuplicado(Usuario usuario) {
        usuarioRepository.findByEmailIgnoreCase(usuario.getEmail())
                .filter(outro -> !outro.getId().equals(usuario.getId()))
                .ifPresent(outro -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail ja cadastrado");
                });
    }

    private static String texto(String valor) {
        if (valor == null || valor.isBlank()) return null;
        return valor.trim();
    }
}

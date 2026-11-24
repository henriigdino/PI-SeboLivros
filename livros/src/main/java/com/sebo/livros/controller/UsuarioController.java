package com.sebo.livros.controller;

import com.sebo.livros.dto.UsuarioCadastroDTO;
import com.sebo.livros.dto.UsuarioRespostaDTO;
import com.sebo.livros.entity.Usuario;
import com.sebo.livros.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioRespostaDTO> listarTodos() {
        return usuarioService.listarTodos().stream()
                .map(this::converterParaDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public UsuarioRespostaDTO buscarPorId(@PathVariable Long id) {
        return converterParaDTO(usuarioService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado")));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioRespostaDTO cadastrar(@Valid @RequestBody UsuarioCadastroDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
        usuario.setSenha(dto.senha());
        usuario.setTipo(dto.tipo());
        return converterParaDTO(usuarioService.salvar(usuario));
    }

    @PutMapping("/{id}")
    public UsuarioRespostaDTO atualizar(@PathVariable Long id,
                                        @Valid @RequestBody UsuarioCadastroDTO dto) {
        Usuario usuario = usuarioService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado"));
        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
        usuario.setTipo(dto.tipo());
        return converterParaDTO(usuarioService.atualizar(usuario, dto.senha()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        usuarioService.excluir(id);
    }

    private UsuarioRespostaDTO converterParaDTO(Usuario usuario) {
        UsuarioRespostaDTO dto = new UsuarioRespostaDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setEmail(usuario.getEmail());
        dto.setTipo(usuario.getTipo());
        dto.setAtivo(usuario.getAtivo());
        return dto;
    }
}

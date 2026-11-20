
package com.sebo.livros.controller;

import com.sebo.livros.dto.UsuarioCadastroDTO;
import com.sebo.livros.dto.UsuarioRespostaDTO;
import com.sebo.livros.entity.Usuario;
import com.sebo.livros.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "http://localhost:4200")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioRespostaDTO> listarTodos() {
        return usuarioService.listarTodos().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioRespostaDTO> buscarPorId(
            @PathVariable Long id) {

        return usuarioService.buscarPorId(id)
                .map(usuario -> ResponseEntity.ok(converterParaDTO(usuario)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public UsuarioRespostaDTO cadastrar(
            @RequestBody UsuarioCadastroDTO dto) {

        Usuario usuario = new Usuario();

        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(dto.getSenha());
        usuario.setTipo(dto.getTipo());

        return converterParaDTO(usuarioService.salvar(usuario));
    }


    @PutMapping("/{id}")
    public ResponseEntity<UsuarioRespostaDTO> atualizar(
            @PathVariable Long id,
            @RequestBody UsuarioCadastroDTO dto) {

        return usuarioService.buscarPorId(id)
                .map(usuario -> {
                    usuario.setNome(dto.getNome());
                    usuario.setEmail(dto.getEmail());
                    usuario.setTipo(dto.getTipo());

                    Usuario atualizado = usuarioService.atualizar(
                            usuario,
                            dto.getSenha()
                    );

                    return ResponseEntity.ok(converterParaDTO(atualizado));
                })
                .orElse(ResponseEntity.notFound().build());
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        if (usuarioService.buscarPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        usuarioService.excluir(id);
        return ResponseEntity.noContent().build();
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

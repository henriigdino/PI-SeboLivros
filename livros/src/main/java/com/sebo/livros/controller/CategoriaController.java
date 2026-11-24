package com.sebo.livros.controller;

import com.sebo.livros.dto.CategoriaCadastroDTO;
import com.sebo.livros.dto.CategoriaRespostaDTO;
import com.sebo.livros.entity.Categoria;
import com.sebo.livros.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {
    private final CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    @GetMapping
    public List<CategoriaRespostaDTO> listar() {
        return service.listar().stream().map(this::resposta).toList();
    }

    @GetMapping("/{id}")
    public CategoriaRespostaDTO buscar(@PathVariable Long id) {
        return resposta(service.buscar(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaRespostaDTO cadastrar(@Valid @RequestBody CategoriaCadastroDTO dto) {
        return resposta(service.salvar(null, dto));
    }

    @PutMapping("/{id}")
    public CategoriaRespostaDTO atualizar(@PathVariable Long id, @Valid @RequestBody CategoriaCadastroDTO dto) {
        return resposta(service.salvar(id, dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativar(@PathVariable Long id) {
        service.desativar(id);
    }

    private CategoriaRespostaDTO resposta(Categoria categoria) {
        return new CategoriaRespostaDTO(
                categoria.getId(),
                categoria.getNome(),
                categoria.getDescricao(),
                categoria.getAtivo());
    }
}

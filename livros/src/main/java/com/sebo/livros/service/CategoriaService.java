package com.sebo.livros.service;

import com.sebo.livros.dto.CategoriaCadastroDTO;
import com.sebo.livros.entity.Categoria;
import com.sebo.livros.repository.CategoriaRepository;
import com.sebo.livros.repository.LivroRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaService {
    private final CategoriaRepository categorias;
    private final LivroRepository livros;

    public CategoriaService(CategoriaRepository categorias, LivroRepository livros) {
        this.categorias = categorias;
        this.livros = livros;
    }

    @Transactional(readOnly = true)
    public List<Categoria> listar() {
        return categorias.findAllByAtivoTrueOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public Categoria buscar(Long id) {
        Categoria categoria = categorias.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria nao encontrada"));
        if (!Boolean.TRUE.equals(categoria.getAtivo())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria nao encontrada");
        }
        return categoria;
    }

    @Transactional
    public Categoria salvar(Long id, CategoriaCadastroDTO dto) {
        if (dto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados da categoria obrigatorios");
        }

        String nome = texto(dto.nome());
        if (nome == null || nome.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome obrigatorio (ate 100 caracteres)");
        }
        String descricao = texto(dto.descricao());
        if (descricao != null && descricao.length() > 500) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Descricao muito longa (ate 500 caracteres)");
        }

        Categoria categoria;
        if (id == null) {
            // Reaproveita uma categoria desativada para preservar os ids usados pelos livros.
            Optional<Categoria> existente = categorias.findByNomeIgnoreCase(nome);
            if (existente.isPresent() && Boolean.TRUE.equals(existente.get().getAtivo())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Categoria já cadastrada");
            }
            categoria = existente.orElseGet(Categoria::new);
        } else {
            categoria = buscar(id);
            Optional<Categoria> duplicata = categorias.findByNomeIgnoreCase(nome);
            if (duplicata.isPresent() && !duplicata.get().getId().equals(id)
                    && Boolean.TRUE.equals(duplicata.get().getAtivo())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Categoria já cadastrada");
            }
        }

        categoria.setNome(nome);
        categoria.setDescricao(descricao);
        categoria.setAtivo(true);
        return categorias.save(categoria);
    }

    @Transactional
    public void desativar(Long id) {
        Categoria categoria = buscar(id);
        if (livros.existsByCategoriaIdAndAtivoTrue(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Não é possível excluir: existem livros ativos nesta categoria");
        }
        categoria.setAtivo(false);
        categorias.save(categoria);
    }

    private static String texto(String valor) {
        if (valor == null || valor.isBlank()) return null;
        return valor.trim();
    }
}

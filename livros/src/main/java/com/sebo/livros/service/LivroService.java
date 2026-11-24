package com.sebo.livros.service;

import com.sebo.livros.entity.Livro;
import com.sebo.livros.repository.CategoriaRepository;
import com.sebo.livros.repository.LivroRepository;
import com.sebo.livros.repository.VendaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class LivroService {

    private final LivroRepository livroRepository;
    private final CategoriaRepository categoriaRepository;
    private final VendaRepository vendaRepository;

    public LivroService(LivroRepository livroRepository, CategoriaRepository categoriaRepository,
                        VendaRepository vendaRepository) {
        this.livroRepository = livroRepository;
        this.categoriaRepository = categoriaRepository;
        this.vendaRepository = vendaRepository;
    }

    public List<Livro> listarTodos() {
        return livroRepository.findAll();
    }

    public Optional<Livro> buscarPorId(Long id) {
        return livroRepository.findById(id);
    }

    // Valida os campos principais antes de salvar no H2.
    public Livro salvar(Livro livro) {
        if (livro.getTitulo() == null || livro.getTitulo().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Titulo obrigatorio.");
        }
        if (livro.getAutor() == null || livro.getAutor().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Autor obrigatorio.");
        }
        if (livro.getCategoriaId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoria obrigatoria.");
        }
        if (!categoriaRepository.existsByIdAndAtivoTrue(livro.getCategoriaId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoria inexistente ou inativa.");
        }
        if (livro.getPreco() == null || livro.getPreco().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Preco deve ser maior que zero.");
        }

        if (livro.getStatus() == null || livro.getStatus().isBlank()) {
            livro.setStatus("DISPONIVEL");
        }
        if (!List.of("DISPONIVEL", "RESERVADO", "VENDIDO").contains(livro.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status do livro invalido.");
        }
        if (livro.getAtivo() == null) {
            livro.setAtivo(true);
        }
        return livroRepository.save(livro);
    }

    // Exclusao fisica permitida apenas quando o exemplar nao tem historico de vendas.
    public void excluir(Long id) {
        if (!livroRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro nao encontrado");
        }
        if (vendaRepository.existsByLivroId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Nao e possivel excluir um exemplar que possui historico de vendas");
        }
        livroRepository.deleteById(id);
    }
}

package com.sebo.livros.controller;

import com.sebo.livros.dto.LivroCadastroDTO;
import com.sebo.livros.dto.LivroRespostaDTO;
import com.sebo.livros.entity.Livro;
import com.sebo.livros.repository.CategoriaRepository;
import com.sebo.livros.repository.VendaRepository;
import com.sebo.livros.service.LivroService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/livros")
public class LivroController {

    private final LivroService livroService;
    private final CategoriaRepository categoriaRepository;
    private final VendaRepository vendaRepository;

    public LivroController(LivroService livroService, CategoriaRepository categoriaRepository,
                           VendaRepository vendaRepository) {
        this.livroService = livroService;
        this.categoriaRepository = categoriaRepository;
        this.vendaRepository = vendaRepository;
    }

    private Livro converterParaEntidade(LivroCadastroDTO dto) {
        Livro livro = new Livro();
        livro.setTitulo(dto.getTitulo());
        livro.setAutor(dto.getAutor());
        livro.setIsbn(dto.getIsbn());
        livro.setEditora(dto.getEditora());
        livro.setAno(dto.getAno());
        livro.setCategoriaId(dto.getCategoriaId());
        livro.setPreco(dto.getPreco());
        livro.setStatus(dto.getStatus());
        livro.setDescricao(dto.getDescricao());
        return livro;
    }

    private LivroRespostaDTO converterParaResposta(Livro livro) {
        LivroRespostaDTO dto = new LivroRespostaDTO();
        dto.setId(livro.getId());
        dto.setTitulo(livro.getTitulo());
        dto.setAutor(livro.getAutor());
        dto.setIsbn(livro.getIsbn());
        dto.setEditora(livro.getEditora());
        dto.setAno(livro.getAno());
        dto.setCategoriaId(livro.getCategoriaId());
        dto.setCategoriaNome(livro.getCategoriaId() == null ? null
                : categoriaRepository.findById(livro.getCategoriaId())
                        .map(categoria -> categoria.getNome())
                        .orElse(null));
        dto.setPreco(livro.getPreco());
        dto.setStatus(livro.getStatus());
        dto.setDescricao(livro.getDescricao());
        dto.setAtivo(livro.getAtivo());
        dto.setPossuiHistoricoVendas(livro.getId() != null
                && vendaRepository.existsByLivroId(livro.getId()));
        return dto;
    }

    @GetMapping
    public List<LivroRespostaDTO> listar() {
        return livroService.listarTodos().stream()
                .map(this::converterParaResposta).toList();
    }

    @GetMapping("/{id}")
    public LivroRespostaDTO buscar(@PathVariable Long id) {
        Livro livro = livroService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro nao encontrado"));
        return converterParaResposta(livro);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LivroRespostaDTO cadastrar(@Valid @RequestBody LivroCadastroDTO dto) {
        Livro livro = converterParaEntidade(dto);
        // Todo livro novo comeca disponivel, independentemente do formulario.
        livro.setStatus("DISPONIVEL");
        return converterParaResposta(livroService.salvar(livro));
    }

    @PutMapping("/{id}")
    public LivroRespostaDTO atualizar(@PathVariable Long id, @Valid @RequestBody LivroCadastroDTO dto) {
        Livro livro = livroService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro nao encontrado"));
        livro.setTitulo(dto.getTitulo());
        livro.setAutor(dto.getAutor());
        livro.setIsbn(dto.getIsbn());
        livro.setEditora(dto.getEditora());
        livro.setAno(dto.getAno());
        livro.setCategoriaId(dto.getCategoriaId());
        livro.setPreco(dto.getPreco());
        livro.setDescricao(dto.getDescricao());
        // Status nao muda aqui: ele sera controlado pelas regras de venda/reserva.
        return converterParaResposta(livroService.salvar(livro));
    }

    // Envia uma capa para um livro existente usando multipart/form-data.
    // O limite de 1 MB respeita a configuracao padrao do Spring Boot.
    @PostMapping(value = "/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enviarFoto(@PathVariable Long id, @RequestParam("arquivo") MultipartFile arquivo)
            throws IOException {
        Livro livro = livroService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro nao encontrado"));

        if (arquivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione uma imagem.");
        }
        if (arquivo.getSize() > 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "A foto deve ter ate 1 MB.");
        }

        byte[] bytes = arquivo.getBytes();
        // Nao confiar somente na extensao ou no Content-Type enviado pelo navegador.
        MediaType tipo = tipoDaFoto(bytes);
        if (!tipo.equals(MediaType.IMAGE_JPEG)
                && !tipo.equals(MediaType.IMAGE_PNG)
                && !tipo.equals(MediaType.parseMediaType("image/webp"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use JPG, PNG ou WebP.");
        }

        livro.setFoto(bytes);
        livroService.salvar(livro);
    }

    // Remove a capa de um livro existente, mantendo os dados bibliograficos.
    @DeleteMapping("/{id}/foto")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerFoto(@PathVariable Long id) {
        Livro livro = livroService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro nao encontrado"));
        livro.setFoto(null);
        livroService.salvar(livro);
    }


    // A capa sera servida em uma URL propria, sem incluir os bytes no JSON.
    @GetMapping("/{id}/foto")
    public ResponseEntity<byte[]> buscarFoto(@PathVariable Long id) {
        Livro livro = livroService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro nao encontrado"));
        byte[] foto = livro.getFoto();
        if (foto == null || foto.length == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(tipoDaFoto(foto))
                .body(foto);
    }

    // Reconhece formatos comuns de imagem a partir da assinatura binaria.
    private MediaType tipoDaFoto(byte[] bytes) {
        if (bytes.length >= 3 && (bytes[0] & 255) == 255
                && (bytes[1] & 255) == 216 && (bytes[2] & 255) == 255) {
            return MediaType.IMAGE_JPEG;
        }
        if (bytes.length >= 4 && (bytes[0] & 255) == 137
                && bytes[1] == 80 && bytes[2] == 78 && bytes[3] == 71) {
            return MediaType.IMAGE_PNG;
        }
        if (bytes.length >= 12 && bytes[0] == 82 && bytes[1] == 73
                && bytes[2] == 70 && bytes[3] == 70
                && bytes[8] == 87 && bytes[9] == 69
                && bytes[10] == 66 && bytes[11] == 80) {
            return MediaType.parseMediaType("image/webp");
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        if (livroService.buscarPorId(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro nao encontrado");
        }
        livroService.excluir(id);
    }
}

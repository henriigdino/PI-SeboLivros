package com.sebo.livros.controller;

import com.sebo.livros.dto.FornecedorCadastroDTO;
import com.sebo.livros.dto.FornecedorRespostaDTO;
import com.sebo.livros.entity.Fornecedor;
import com.sebo.livros.service.FornecedorService;
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
@RequestMapping("/api/fornecedores")
public class FornecedorController {

    private final FornecedorService fornecedorService;

    public FornecedorController(FornecedorService fornecedorService) {
        this.fornecedorService = fornecedorService;
    }

    @GetMapping
    public List<FornecedorRespostaDTO> listarTodos() {
        return fornecedorService.listarTodos().stream()
                .map(this::converterParaDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public FornecedorRespostaDTO buscarPorId(@PathVariable Long id) {
        return converterParaDTO(fornecedorService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fornecedor nao encontrado")));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FornecedorRespostaDTO cadastrar(@Valid @RequestBody FornecedorCadastroDTO dto) {
        return converterParaDTO(fornecedorService.salvar(paraEntidade(new Fornecedor(), dto)));
    }

    @PutMapping("/{id}")
    public FornecedorRespostaDTO atualizar(@PathVariable Long id,
                                           @Valid @RequestBody FornecedorCadastroDTO dto) {
        Fornecedor existente = fornecedorService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fornecedor nao encontrado"));
        return converterParaDTO(fornecedorService.salvar(paraEntidade(existente, dto)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        fornecedorService.excluir(id);
    }

    private Fornecedor paraEntidade(Fornecedor fornecedor, FornecedorCadastroDTO dto) {
        fornecedor.setNome(dto.nome());
        fornecedor.setDocumento(dto.documento());
        fornecedor.setEmail(dto.email());
        fornecedor.setTelefone(dto.telefone());
        fornecedor.setEndereco(dto.endereco());
        return fornecedor;
    }

    private FornecedorRespostaDTO converterParaDTO(Fornecedor fornecedor) {
        FornecedorRespostaDTO dto = new FornecedorRespostaDTO();
        dto.setId(fornecedor.getId());
        dto.setNome(fornecedor.getNome());
        dto.setDocumento(fornecedor.getDocumento());
        dto.setEmail(fornecedor.getEmail());
        dto.setTelefone(fornecedor.getTelefone());
        dto.setEndereco(fornecedor.getEndereco());
        dto.setAtivo(fornecedor.getAtivo());
        return dto;
    }
}

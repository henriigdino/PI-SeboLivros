
package com.sebo.livros.controller;

import com.sebo.livros.dto.FornecedorCadastroDTO;
import com.sebo.livros.dto.FornecedorRespostaDTO;
import com.sebo.livros.entity.Fornecedor;
import com.sebo.livros.service.FornecedorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fornecedores")
@CrossOrigin(origins = "http://localhost:4200")
public class FornecedorController {

    private final FornecedorService fornecedorService;

    public FornecedorController(FornecedorService fornecedorService) {
        this.fornecedorService = fornecedorService;
    }

    @GetMapping
    public List<FornecedorRespostaDTO> listarTodos() {
        return fornecedorService.listarTodos()
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<FornecedorRespostaDTO> buscarPorId(
            @PathVariable Long id) {

        return fornecedorService.buscarPorId(id)
                .map(fornecedor -> ResponseEntity.ok(converterParaDTO(fornecedor)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public FornecedorRespostaDTO cadastrar(
            @RequestBody FornecedorCadastroDTO dto) {

        Fornecedor fornecedor = new Fornecedor();

        fornecedor.setNome(dto.getNome());
        fornecedor.setDocumento(dto.getDocumento());
        fornecedor.setEmail(dto.getEmail());
        fornecedor.setTelefone(dto.getTelefone());
        fornecedor.setEndereco(dto.getEndereco());

        return converterParaDTO(fornecedorService.salvar(fornecedor));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FornecedorRespostaDTO> atualizar(
            @PathVariable Long id,
            @RequestBody FornecedorCadastroDTO dto) {

        return fornecedorService.buscarPorId(id)
                .map(fornecedor -> {
                    fornecedor.setNome(dto.getNome());
                    fornecedor.setDocumento(dto.getDocumento());
                    fornecedor.setEmail(dto.getEmail());
                    fornecedor.setTelefone(dto.getTelefone());
                    fornecedor.setEndereco(dto.getEndereco());

                    Fornecedor atualizado = fornecedorService.salvar(fornecedor);

                    return ResponseEntity.ok(converterParaDTO(atualizado));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        if (fornecedorService.buscarPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        fornecedorService.excluir(id);
        return ResponseEntity.noContent().build();
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

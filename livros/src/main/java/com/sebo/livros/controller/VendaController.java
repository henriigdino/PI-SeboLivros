package com.sebo.livros.controller;

import com.sebo.livros.dto.ReembolsoDTO;
import com.sebo.livros.dto.VendaCadastroDTO;
import com.sebo.livros.dto.VendaRespostaDTO;
import com.sebo.livros.entity.Venda;
import com.sebo.livros.service.VendaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/vendas")
public class VendaController {
    private final VendaService service;

    public VendaController(VendaService service) {
        this.service = service;
    }

    @GetMapping
    public List<VendaRespostaDTO> listar() {
        return service.listar().stream().map(this::resposta).toList();
    }

    @GetMapping("/{id}")
    public VendaRespostaDTO buscar(@PathVariable Long id) {
        return resposta(service.buscar(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VendaRespostaDTO registrar(@Valid @RequestBody VendaCadastroDTO dto, Authentication auth) {
        exigirAdmin(auth);
        return resposta(service.registrar(dto));
    }

    @PostMapping("/{id}/reembolso")
    public VendaRespostaDTO reembolsar(@PathVariable Long id,
                                       @Valid @RequestBody(required = false) ReembolsoDTO dto,
                                       Authentication auth) {
        exigirAdmin(auth);
        return resposta(service.reembolsar(id, dto));
    }

    private static void exigirAdmin(Authentication auth) {
        if (auth == null || auth.getAuthorities().stream()
                .noneMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas ADMIN pode registrar vendas");
        }
    }

    private VendaRespostaDTO resposta(Venda v) {
        return new VendaRespostaDTO(v.getId(), v.getData(), v.getClienteId(), v.getClienteNome(),
                v.getLivroId(), v.getLivroTitulo(), v.getValor(), v.getFormaPagamento(), v.getStatus(),
                v.getDataReembolso(), v.getMotivoReembolso());
    }
}

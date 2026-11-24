package com.sebo.livros.controller;

import com.sebo.livros.dto.ClienteCadastroDTO;
import com.sebo.livros.dto.ClienteRespostaDTO;
import com.sebo.livros.entity.Cliente;
import com.sebo.livros.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/api/clientes")
public class ClienteController {
    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @GetMapping
    public List<ClienteRespostaDTO> listar() {
        return service.listar().stream().map(this::resposta).toList();
    }

    @GetMapping("/{id}")
    public ClienteRespostaDTO buscar(@PathVariable Long id) {
        return resposta(service.buscar(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteRespostaDTO cadastrar(@Valid @RequestBody ClienteCadastroDTO dto, Authentication authentication) {
        exigirAdmin(authentication);
        return resposta(service.salvar(null, dto));
    }

    @PutMapping("/{id}")
    public ClienteRespostaDTO atualizar(@PathVariable Long id, @Valid @RequestBody ClienteCadastroDTO dto,
                                        Authentication authentication) {
        exigirAdmin(authentication);
        return resposta(service.salvar(id, dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativar(@PathVariable Long id, Authentication authentication) {
        exigirAdmin(authentication);
        service.desativar(id);
    }

    private static void exigirAdmin(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities().stream()
                .noneMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas ADMIN pode alterar clientes");
        }
    }

    private ClienteRespostaDTO resposta(Cliente c) {
        return new ClienteRespostaDTO(c.getId(), c.getNome(), c.getTipoPessoa(), c.getDocumento(),
                c.getRg(), c.getInscricaoEstadual(), c.getEmail(), c.getTelefone(), c.getCep(),
                c.getLogradouro(), c.getNumero(), c.getComplemento(), c.getBairro(),
                c.getCidade(), c.getUf(), c.getAtivo());
    }
}

package com.sebo.livros.service;

import com.sebo.livros.dto.ClienteCadastroDTO;
import com.sebo.livros.entity.Cliente;
import com.sebo.livros.repository.ClienteRepository;
import com.sebo.livros.util.Documentos;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
public class ClienteService {
    private static final Set<String> UFS_COM_IE = Set.of("SP", "MG", "RJ");

    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
    }

    public List<Cliente> listar() {
        return repository.findByAtivoTrueOrderByNomeAsc();
    }

    public Cliente buscar(Long id) {
        return repository.findById(id)
                .filter(c -> Boolean.TRUE.equals(c.getAtivo()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente nao encontrado"));
    }

    @Transactional
    public Cliente salvar(Long id, ClienteCadastroDTO dto) {
        if (dto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados do cliente obrigatorios");
        }
        Cliente cliente = id == null ? new Cliente() : buscar(id);
        String nome = texto(dto.nome());
        String tipoPessoa = texto(dto.tipoPessoa());
        if (tipoPessoa != null) {
            tipoPessoa = tipoPessoa.toUpperCase(Locale.ROOT);
        }
        String documento = Documentos.digitos(dto.documento());
        String uf = texto(dto.uf());
        if (uf != null) {
            uf = uf.toUpperCase(Locale.ROOT);
        }

        if (nome == null || nome.length() > 150) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome obrigatorio (ate 150 caracteres)");
        }
        if (!"FISICA".equals(tipoPessoa) && !"JURIDICA".equals(tipoPessoa)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de pessoa obrigatorio ou invalido");
        }
        // Qualquer sequencia de 11 ou 14 digitos e aceita, independentemente do
        // tipo de pessoa e dos digitos verificadores.
        if (!Documentos.documentoValido(documento)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CPF ou CNPJ invalido");
        }
        if (uf != null && !uf.matches("[A-Z]{2}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UF deve conter 2 letras");
        }
        // No recadastro, reaproveita o registro desativado para preservar vendas e historico.
        Cliente existente = repository.findByDocumento(documento).orElse(null);
        if (existente != null && !existente.getId().equals(cliente.getId())) {
            if (id == null && !Boolean.TRUE.equals(existente.getAtivo())) {
                cliente = existente;
                cliente.setAtivo(true);
            } else {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF/CNPJ já cadastrado");
            }
        }

        String rg = texto(dto.rg());
        String ie = texto(dto.inscricaoEstadual());
        if ("FISICA".equals(tipoPessoa)) {
            ie = null;
            if (rg != null && !rg.matches("[0-9.\\-]{5,20}[0-9Xx]")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de RG invalido");
            }
        } else {
            rg = null;
            if (ie != null && !"ISENTO".equalsIgnoreCase(ie)) {
                if (!ie.matches("[0-9.\\-/]{8,20}")) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Formato de inscricao estadual invalido");
                }
                if (!UFS_COM_IE.contains(uf)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Inscricao estadual permitida apenas para SP, MG e RJ");
                }
            }
        }
        String cep = Documentos.digitos(dto.cep());
        if (!cep.isEmpty() && cep.length() != 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CEP deve ter 8 digitos");
        }
        String email = texto(dto.email());
        if (email != null && (email.length() > 150 || !email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail invalido");
        }
        if (email != null) {
            email = email.toLowerCase(Locale.ROOT);
        }
        String telefone = texto(dto.telefone());
        validarTamanho(telefone, 11, "Telefone");
        validarTamanho(dto.logradouro(), 150, "Logradouro");
        validarTamanho(dto.numero(), 20, "Numero");
        validarTamanho(dto.complemento(), 100, "Complemento");
        validarTamanho(dto.bairro(), 100, "Bairro");
        validarTamanho(dto.cidade(), 100, "Cidade");

        cliente.setNome(nome);
        cliente.setTipoPessoa(tipoPessoa);
        cliente.setDocumento(documento);
        cliente.setRg(rg);
        cliente.setInscricaoEstadual(ie == null ? null : ie.toUpperCase());
        cliente.setEmail(email);
        cliente.setTelefone(telefone);
        cliente.setCep(cep.isEmpty() ? null : cep);
        cliente.setLogradouro(texto(dto.logradouro()));
        cliente.setNumero(texto(dto.numero()));
        cliente.setComplemento(texto(dto.complemento()));
        cliente.setBairro(texto(dto.bairro()));
        cliente.setCidade(texto(dto.cidade()));
        cliente.setUf(uf);

        validarDuplicados(cliente);
        return repository.save(cliente);
    }

    @Transactional
    public void desativar(Long id) {
        Cliente cliente = buscar(id);
        // Exclusao logica: preservar integridade de futuras vendas.
        cliente.setAtivo(false);
        repository.save(cliente);
    }

    /** Nome, RG, e-mail, telefone e endereco nao podem se repetir entre clientes. */
    private void validarDuplicados(Cliente cliente) {
        validarDuplicado(repository.findByNomeIgnoreCase(cliente.getNome()), cliente, "Nome já cadastrado");
        if (cliente.getRg() != null) {
            validarDuplicado(repository.findByRgIgnoreCase(cliente.getRg()), cliente, "RG já cadastrado");
        }
        if (cliente.getEmail() != null) {
            validarDuplicado(repository.findByEmailIgnoreCase(cliente.getEmail()), cliente, "E-mail já cadastrado");
        }
        if (cliente.getTelefone() != null) {
            validarDuplicado(repository.findByTelefone(cliente.getTelefone()), cliente, "Telefone já cadastrado");
        }
        if (temEndereco(cliente)) {
            String alvo = endereco(cliente);
            Optional<Cliente> igual = repository.findAll().stream()
                    .filter(outro -> !outro.getId().equals(cliente.getId()))
                    .filter(outro -> temEndereco(outro) && alvo.equals(endereco(outro)))
                    .findFirst();
            if (igual.isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Endereço já cadastrado");
            }
        }
    }

    private static void validarDuplicado(Optional<Cliente> existente, Cliente atual, String mensagem) {
        existente.filter(outro -> !outro.getId().equals(atual.getId()))
                .ifPresent(outro -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, mensagem);
                });
    }

    private static boolean temEndereco(Cliente cliente) {
        return !endereco(cliente).replace("|", "").isBlank();
    }

    private static String endereco(Cliente cliente) {
        return String.join("|",
                nulo(cliente.getCep()), nulo(cliente.getLogradouro()), nulo(cliente.getNumero()),
                nulo(cliente.getComplemento()), nulo(cliente.getBairro()), nulo(cliente.getCidade()),
                nulo(cliente.getUf())).toLowerCase(Locale.ROOT);
    }

    private static String nulo(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private static String texto(String valor) {
        if (valor == null || valor.isBlank()) return null;
        return valor.trim();
    }

    private static void validarTamanho(String valor, int maximo, String campo) {
        if (texto(valor) != null && texto(valor).length() > maximo) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, campo + " muito longo");
        }
    }
}

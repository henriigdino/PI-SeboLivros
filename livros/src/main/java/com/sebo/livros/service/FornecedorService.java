package com.sebo.livros.service;

import com.sebo.livros.entity.Fornecedor;
import com.sebo.livros.repository.FornecedorRepository;
import com.sebo.livros.util.Documentos;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;

    public FornecedorService(FornecedorRepository fornecedorRepository) {
        this.fornecedorRepository = fornecedorRepository;
    }

    public List<Fornecedor> listarTodos() {
        return fornecedorRepository.findAll();
    }

    public Optional<Fornecedor> buscarPorId(Long id) {
        return fornecedorRepository.findById(id);
    }

    @Transactional
    public Fornecedor salvar(Fornecedor fornecedor) {
        String nome = texto(fornecedor.getNome());
        String documento = Documentos.digitos(fornecedor.getDocumento());
        if (nome == null || nome.length() > 150) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome obrigatorio (ate 150 caracteres)");
        }
        if (!Documentos.documentoValido(documento)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CPF/CNPJ invalido");
        }

        String email = texto(fornecedor.getEmail());
        if (email != null && (email.length() > 100 || !email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail invalido");
        }
        if (email != null) {
            email = email.toLowerCase(Locale.ROOT);
        }
        String telefone = texto(fornecedor.getTelefone());
        if (telefone != null && telefone.length() > 11) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Telefone deve ter ate 11 caracteres");
        }
        String endereco = texto(fornecedor.getEndereco());
        if (endereco != null && endereco.length() > 150) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Endereco muito longo");
        }

        validarDuplicado(fornecedorRepository.findByDocumento(documento), fornecedor.getId(),
                "CPF/CNPJ já cadastrado");
        if (email != null) {
            validarDuplicado(fornecedorRepository.findByEmailIgnoreCase(email), fornecedor.getId(),
                    "E-mail já cadastrado");
        }
        if (telefone != null) {
            validarDuplicado(fornecedorRepository.findByTelefone(telefone), fornecedor.getId(),
                    "Telefone já cadastrado");
        }

        fornecedor.setNome(nome);
        fornecedor.setDocumento(documento);
        fornecedor.setEmail(email);
        fornecedor.setTelefone(telefone);
        fornecedor.setEndereco(endereco);
        if (fornecedor.getAtivo() == null) {
            fornecedor.setAtivo(true);
        }
        return fornecedorRepository.save(fornecedor);
    }

    @Transactional
    public void excluir(Long id) {
        if (!fornecedorRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fornecedor nao encontrado");
        }
        fornecedorRepository.deleteById(id);
    }

    private static void validarDuplicado(Optional<Fornecedor> existente, Long idAtual, String mensagem) {
        existente.filter(outro -> !outro.getId().equals(idAtual))
                .ifPresent(outro -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, mensagem);
                });
    }

    private static String texto(String valor) {
        if (valor == null || valor.isBlank()) return null;
        return valor.trim();
    }
}

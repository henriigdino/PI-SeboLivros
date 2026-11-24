package com.sebo.livros.service;

import com.sebo.livros.dto.ReembolsoDTO;
import com.sebo.livros.dto.VendaCadastroDTO;
import com.sebo.livros.entity.Cliente;
import com.sebo.livros.entity.Livro;
import com.sebo.livros.entity.Venda;
import com.sebo.livros.repository.ClienteRepository;
import com.sebo.livros.repository.VendaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class VendaService {
    private static final Set<String> FORMAS_PAGAMENTO = Set.of(
            "DINHEIRO", "PIX", "CARTAO_CREDITO", "CARTAO_DEBITO", "BOLETO", "TRANSFERENCIA");

    private final VendaRepository vendas;
    private final ClienteRepository clientes;
    private final EntityManager entityManager;

    public VendaService(VendaRepository vendas, ClienteRepository clientes, EntityManager entityManager) {
        this.vendas = vendas;
        this.clientes = clientes;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public List<Venda> listar() {
        return vendas.findAllByOrderByDataDesc();
    }

    @Transactional(readOnly = true)
    public Venda buscar(Long id) {
        return vendas.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venda nao encontrada"));
    }

    @Transactional
    public Venda registrar(VendaCadastroDTO dto) {
        if (dto == null || dto.clienteId() == null || dto.livroId() == null
                || dto.clienteId() <= 0 || dto.livroId() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe cliente e livro validos");
        }

        Cliente cliente = clientes.findById(dto.clienteId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente nao encontrado"));
        if (!Boolean.TRUE.equals(cliente.getAtivo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cliente inativo nao pode comprar");
        }

        // Bloqueio transacional para duas compras simultaneas do mesmo exemplar.
        Livro livro = entityManager.find(Livro.class, dto.livroId(), LockModeType.PESSIMISTIC_WRITE);
        if (livro == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro nao encontrado");
        }
        if (!Boolean.TRUE.equals(livro.getAtivo()) || !"DISPONIVEL".equals(livro.getStatus())
                || vendas.existsByLivroIdAndStatus(livro.getId(), "REALIZADA")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Livro indisponivel ou ja vendido");
        }
        if (livro.getPreco() == null || livro.getPreco().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Livro sem preco valido");
        }

        String formaPagamento = texto(dto.formaPagamento());
        if (formaPagamento == null || !FORMAS_PAGAMENTO.contains(formaPagamento)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Forma de pagamento invalida");
        }
        if (dto.valor() != null && dto.valor().compareTo(livro.getPreco()) != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O valor informado deve corresponder ao preco do livro");
        }

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime dataVenda = dto.data() == null ? agora : dto.data();
        if (dataVenda.isAfter(agora.plusMinutes(5))
                || dataVenda.isBefore(LocalDateTime.of(2000, 1, 1, 0, 0))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data da venda fora do intervalo permitido");
        }

        Venda venda = new Venda();
        venda.setData(dataVenda);
        venda.setClienteId(cliente.getId());
        venda.setClienteNome(cliente.getNome());
        venda.setLivroId(livro.getId());
        venda.setLivroTitulo(livro.getTitulo());
        // O valor da venda e o preco atual do exemplar, nunca um valor enviado pelo cliente.
        venda.setValor(livro.getPreco());
        venda.setFormaPagamento(formaPagamento);
        venda.setStatus("REALIZADA");

        livro.setStatus("VENDIDO");
        // O JPA grava o novo status e a venda na mesma transacao.
        return vendas.saveAndFlush(venda);
    }

    @Transactional
    public Venda reembolsar(Long id, ReembolsoDTO dto) {
        Venda venda = entityManager.find(Venda.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (venda == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Venda nao encontrada");
        }
        if (!"REALIZADA".equals(venda.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta venda ja foi reembolsada ou cancelada");
        }

        Livro livro = entityManager.find(Livro.class, venda.getLivroId(), LockModeType.PESSIMISTIC_WRITE);
        if (livro == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro da venda nao encontrado");
        }
        if (!"VENDIDO".equals(livro.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "O exemplar nao esta mais vendido; revise o acervo antes de reembolsar");
        }

        // Motivo é texto livre: apenas remove espaços, sem alterar caixa ou acentos.
        String motivo = dto == null || dto.motivo() == null ? null : dto.motivo().trim();
        if (motivo != null && motivo.isBlank()) motivo = null;
        if (motivo != null && motivo.length() > 300) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Motivo muito longo (ate 300 caracteres)");
        }

        venda.setStatus("REEMBOLSADA");
        venda.setDataReembolso(LocalDateTime.now());
        venda.setMotivoReembolso(motivo);
        // Devolve o exemplar ao acervo na mesma transacao do estorno.
        livro.setStatus("DISPONIVEL");
        return vendas.saveAndFlush(venda);
    }

    private static String texto(String valor) {
        if (valor == null || valor.isBlank()) return null;
        return valor.trim().toUpperCase(Locale.ROOT);
    }
}

package com.sebo.livros;

import com.sebo.livros.dto.CategoriaCadastroDTO;
import com.sebo.livros.dto.ClienteCadastroDTO;
import com.sebo.livros.dto.ReembolsoDTO;
import com.sebo.livros.dto.VendaCadastroDTO;
import com.sebo.livros.entity.Livro;
import com.sebo.livros.entity.Venda;
import com.sebo.livros.repository.LivroRepository;
import com.sebo.livros.service.CategoriaService;
import com.sebo.livros.service.ClienteService;
import com.sebo.livros.service.LivroService;
import com.sebo.livros.service.VendaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:reembolsotest;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.h2.console.enabled=false",
        "sebo.seed.enabled=false"
})
@Transactional
class ReembolsoServiceTest {

    @Autowired
    private CategoriaService categorias;

    @Autowired
    private ClienteService clientes;

    @Autowired
    private LivroService livros;

    @Autowired
    private VendaService vendas;

    @Autowired
    private LivroRepository livroRepository;

    @Test
    void reembolsoDevolveLivroPermiteNovaVendaEBloqueiaDuplicidade() {
        var categoria = categorias.salvar(null, new CategoriaCadastroDTO("Ficcao", null));
        var cliente = clientes.salvar(null, new ClienteCadastroDTO(
                "Ana", "FISICA", "52998224725", null, null, "ana@teste.com", null,
                null, null, null, null, null, null, "SP"));

        Livro dadosLivro = new Livro();
        dadosLivro.setTitulo("Livro Teste");
        dadosLivro.setAutor("Autor Teste");
        dadosLivro.setCategoriaId(categoria.getId());
        dadosLivro.setPreco(new BigDecimal("29.90"));
        dadosLivro.setStatus("DISPONIVEL");
        Livro livro = livros.salvar(dadosLivro);

        Venda venda = vendas.registrar(
                new VendaCadastroDTO(cliente.getId(), livro.getId(), null, null, "PIX"));
        assertEquals("VENDIDO", livroRepository.findById(livro.getId()).orElseThrow().getStatus());

        Venda reembolsada = vendas.reembolsar(venda.getId(), new ReembolsoDTO("Desistencia"));
        assertEquals("REEMBOLSADA", reembolsada.getStatus());
        assertNotNull(reembolsada.getDataReembolso());
        assertEquals("Desistencia", reembolsada.getMotivoReembolso());
        assertEquals("DISPONIVEL", livroRepository.findById(livro.getId()).orElseThrow().getStatus());

        Venda novaVenda = vendas.registrar(
                new VendaCadastroDTO(cliente.getId(), livro.getId(), null, null, "DINHEIRO"));
        assertNotEquals(venda.getId(), novaVenda.getId());

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> vendas.reembolsar(venda.getId(), new ReembolsoDTO("De novo")));
        assertEquals(409, erro.getStatusCode().value());
    }
}

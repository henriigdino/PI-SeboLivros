package com.sebo.livros;

import com.sebo.livros.dto.CategoriaCadastroDTO;
import com.sebo.livros.dto.ClienteCadastroDTO;
import com.sebo.livros.dto.VendaCadastroDTO;
import com.sebo.livros.entity.Fornecedor;
import com.sebo.livros.entity.Livro;
import com.sebo.livros.entity.Usuario;
import com.sebo.livros.service.CategoriaService;
import com.sebo.livros.service.ClienteService;
import com.sebo.livros.service.FornecedorService;
import com.sebo.livros.service.LivroService;
import com.sebo.livros.service.UsuarioService;
import com.sebo.livros.service.VendaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:validacaotest;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.h2.console.enabled=false",
        "sebo.seed.enabled=false"
})
@Transactional
class ValidacaoServiceTest {

    private static final String CPF = "52998224725";
    private static final String CNPJ = "11222333000181";

    @Autowired private CategoriaService categorias;
    @Autowired private ClienteService clientes;
    @Autowired private FornecedorService fornecedores;
    @Autowired private LivroService livros;
    @Autowired private UsuarioService usuarios;
    @Autowired private VendaService vendas;

    @Test
    void clienteSemTipoPessoaRetorna400EmVezDeNpe() {
        ClienteCadastroDTO dto = new ClienteCadastroDTO(
                "Ana", null, CPF, null, null, null, null, null,
                null, null, null, null, null, "SP");

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> clientes.salvar(null, dto));
        assertEquals(400, erro.getStatusCode().value());
    }

    @Test
    void clienteComInscricaoEstadualForaDeSpMgRjERecusado() {
        ClienteCadastroDTO dto = new ClienteCadastroDTO(
                "Loja", "JURIDICA", CNPJ, null, "123.456.789-0", null, null, null,
                null, null, null, null, null, "MG");

        // MG esta na lista: deve passar.
        assertDoesNotThrow(() -> clientes.salvar(null, dto));

        ClienteCadastroDTO outro = new ClienteCadastroDTO(
                "Outra loja", "JURIDICA", "11444777000161", null, "999.888.777-6", null, null, null,
                null, null, null, null, null, "BA");
        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> clientes.salvar(null, outro));
        assertEquals(400, erro.getStatusCode().value());
    }

    @Test
    void fornecedorValidaDocumentoEBloqueiaDuplicidade() {
        Fornecedor primeiro = fornecedor("Fornecedor Um", CNPJ);
        assertEquals(CNPJ, fornecedores.salvar(primeiro).getDocumento());

        ResponseStatusException duplicado = assertThrows(ResponseStatusException.class,
                () -> fornecedores.salvar(fornecedor("Outro nome", "11.222.333/0001-81")));
        assertEquals(409, duplicado.getStatusCode().value());

        assertEquals("11111111111",
                fornecedores.salvar(fornecedor("Onze digitos", "11111111111")).getDocumento());
        assertEquals("11111111111111",
                fornecedores.salvar(fornecedor("Quatorze digitos", "11.1111-11/111111")).getDocumento());

        ResponseStatusException invalido = assertThrows(ResponseStatusException.class,
                () -> fornecedores.salvar(fornecedor("Invalido", "123")));
        assertEquals(400, invalido.getStatusCode().value());
    }

    @Test
    void usuarioNormalizaPerfilValidaDuplicidadeEProtegeUltimoAdmin() {
        Usuario admin = usuario("Admin", "admin@teste.com", "admin");
        Usuario salvo = usuarios.salvar(admin);
        assertEquals("ADMIN", salvo.getTipo());

        ResponseStatusException duplicado = assertThrows(ResponseStatusException.class,
                () -> usuarios.salvar(usuario("Outro", "ADMIN@teste.com", "FUNCIONARIO")));
        assertEquals(409, duplicado.getStatusCode().value());

        ResponseStatusException perfilInvalido = assertThrows(ResponseStatusException.class,
                () -> usuarios.salvar(usuario("Root", "root@teste.com", "ROOT")));
        assertEquals(400, perfilInvalido.getStatusCode().value());

        ResponseStatusException ultimoAdmin = assertThrows(ResponseStatusException.class,
                () -> usuarios.excluir(salvo.getId()));
        assertEquals(409, ultimoAdmin.getStatusCode().value());

        Usuario segundoAdmin = usuarios.salvar(usuario("Admin 2", "admin2@teste.com", "ADMIN"));
        assertDoesNotThrow(() -> usuarios.excluir(salvo.getId()));
        ResponseStatusException agoraUltimo = assertThrows(ResponseStatusException.class,
                () -> usuarios.excluir(segundoAdmin.getId()));
        assertEquals(409, agoraUltimo.getStatusCode().value());
    }

    @Test
    void vendaValidaFormaPagamentoValorEData() {
        Long clienteId = novoCliente();
        Long livroId = novoLivro();

        ResponseStatusException forma = assertThrows(ResponseStatusException.class,
                () -> vendas.registrar(new VendaCadastroDTO(clienteId, livroId, null, null, "CHEQUE")));
        assertEquals(400, forma.getStatusCode().value());

        ResponseStatusException data = assertThrows(ResponseStatusException.class,
                () -> vendas.registrar(new VendaCadastroDTO(
                        clienteId, livroId, null, LocalDateTime.now().plusDays(1), "PIX")));
        assertEquals(400, data.getStatusCode().value());

        ResponseStatusException valor = assertThrows(ResponseStatusException.class,
                () -> vendas.registrar(new VendaCadastroDTO(
                        clienteId, livroId, new BigDecimal("1.00"), null, "PIX")));
        assertEquals(400, valor.getStatusCode().value());
    }

    @Test
    void livroComVendaNaoPodeSerExcluido() {
        Long livroId = novoLivro();
        vendas.registrar(new VendaCadastroDTO(novoCliente(), livroId, null, null, "PIX"));

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> livros.excluir(livroId));
        assertEquals(409, erro.getStatusCode().value());
    }

    private Long novoCliente() {
        return clientes.salvar(null, new ClienteCadastroDTO(
                "Ana", "FISICA", CPF, null, null, null, null, null,
                null, null, null, null, null, "SP")).getId();
    }

    private Long novoLivro() {
        var categoria = categorias.salvar(null, new CategoriaCadastroDTO("Ficcao", null));
        Livro livro = new Livro();
        livro.setTitulo("Livro Validacao");
        livro.setAutor("Autor");
        livro.setCategoriaId(categoria.getId());
        livro.setPreco(new BigDecimal("30.00"));
        livro.setStatus("DISPONIVEL");
        return livros.salvar(livro).getId();
    }

    private static Fornecedor fornecedor(String nome, String documento) {
        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setNome(nome);
        fornecedor.setDocumento(documento);
        return fornecedor;
    }

    private static Usuario usuario(String nome, String email, String tipo) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha("123456");
        usuario.setTipo(tipo);
        return usuario;
    }
}

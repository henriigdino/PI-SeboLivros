package com.sebo.livros.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Bancos H2 criados antes do reembolso possuem uk_vendas_livro, que impedia
 * uma nova venda do mesmo exemplar. A regra agora vive no VendaService
 * (existsByLivroIdAndStatus), então a constraint antiga e removida.
 */
@Configuration
public class SchemaMigrationConfig {

    @Bean
    public ApplicationRunner removerConstraintUnicaDeVendas(DataSource dataSource) {
        return args -> {
            try (Connection conexao = dataSource.getConnection();
                 Statement comando = conexao.createStatement()) {
                comando.execute("ALTER TABLE vendas DROP CONSTRAINT IF EXISTS uk_vendas_livro");
            } catch (SQLException erro) {
                throw new IllegalStateException("Falha ao migrar a tabela de vendas", erro);
            }
        };
    }
}

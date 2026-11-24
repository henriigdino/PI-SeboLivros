package com.sebo.livros.config;

import com.sebo.livros.entity.Usuario;
import com.sebo.livros.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Cria o primeiro administrador quando o banco esta vazio.
 * Desabilitado em producao e nos testes.
 */
@Component
@ConditionalOnProperty(name = "sebo.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${sebo.seed.admin.nome:Administrador}")
    private String nome;

    @Value("${sebo.seed.admin.email:admin@sebolivros.com}")
    private String email;

    @Value("${sebo.seed.admin.senha:admin123}")
    private String senha;

    public DataSeeder(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarioRepository.countByTipoAndAtivoTrue("ADMIN") > 0) {
            return;
        }
        if (usuarioRepository.findByEmailIgnoreCase(email).isPresent()) {
            log.warn("Ja existe usuario com e-mail {}, mas nenhum ADMIN. Seed ignorado.", email);
            return;
        }

        Usuario admin = new Usuario();
        admin.setNome(nome);
        admin.setEmail(email.trim().toLowerCase(Locale.ROOT));
        admin.setSenha(passwordEncoder.encode(senha));
        admin.setTipo("ADMIN");
        admin.setAtivo(true);
        usuarioRepository.save(admin);

        log.warn("ADMIN inicial criado - e-mail: {} | senha: {}", admin.getEmail(), senha);
    }
}
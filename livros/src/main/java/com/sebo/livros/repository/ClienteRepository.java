package com.sebo.livros.repository;

import com.sebo.livros.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findByAtivoTrueOrderByNomeAsc();
    Optional<Cliente> findByDocumento(String documento);
    Optional<Cliente> findByNomeIgnoreCase(String nome);
    Optional<Cliente> findByRgIgnoreCase(String rg);
    Optional<Cliente> findByEmailIgnoreCase(String email);
    Optional<Cliente> findByTelefone(String telefone);
}

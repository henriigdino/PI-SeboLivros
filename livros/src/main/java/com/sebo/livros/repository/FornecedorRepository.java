package com.sebo.livros.repository;

import com.sebo.livros.entity.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {

    Optional<Fornecedor> findByDocumento(String documento);

    Optional<Fornecedor> findByEmailIgnoreCase(String email);

    Optional<Fornecedor> findByTelefone(String telefone);

    boolean existsByDocumento(String documento);
}

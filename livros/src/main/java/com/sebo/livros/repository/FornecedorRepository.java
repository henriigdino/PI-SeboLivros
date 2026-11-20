
package com.sebo.livros.repository;

import com.sebo.livros.entity.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {

    Optional<Fornecedor> findByDocumento(String documento);

    boolean existsByDocumento(String documento);
}

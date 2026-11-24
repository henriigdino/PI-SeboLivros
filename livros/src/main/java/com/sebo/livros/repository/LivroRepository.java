package com.sebo.livros.repository;

import com.sebo.livros.entity.Livro;
import org.springframework.data.jpa.repository.JpaRepository;

// Fornece as operacoes basicas de acesso ao banco para a entidade Livro.
public interface LivroRepository extends JpaRepository<Livro, Long> {

    boolean existsByCategoriaIdAndAtivoTrue(Long categoriaId);
}

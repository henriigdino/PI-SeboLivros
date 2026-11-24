package com.sebo.livros.repository;

import com.sebo.livros.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findAllByAtivoTrueOrderByNomeAsc();

    Optional<Categoria> findByNomeIgnoreCase(String nome);

    boolean existsByIdAndAtivoTrue(Long id);
}

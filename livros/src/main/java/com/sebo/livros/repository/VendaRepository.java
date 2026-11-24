package com.sebo.livros.repository;

import com.sebo.livros.entity.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VendaRepository extends JpaRepository<Venda, Long> {
    boolean existsByLivroId(Long livroId);
    boolean existsByLivroIdAndStatus(Long livroId, String status);
    List<Venda> findAllByOrderByDataDesc();
}

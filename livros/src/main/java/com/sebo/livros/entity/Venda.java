package com.sebo.livros.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vendas")
@Data
public class Venda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime data;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "livro_id", nullable = false)
    private Long livroId;

    // Guardamos os nomes na data da venda para preservar o historico.
    @Column(nullable = false, length = 150)
    private String clienteNome;

    @Column(nullable = false, length = 200)
    private String livroTitulo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(length = 20)
    private String formaPagamento;

    // Valores possiveis: REALIZADA e REEMBOLSADA.
    @Column(nullable = false, length = 20)
    private String status = "REALIZADA";

    @Column
    private LocalDateTime dataReembolso;

    @Column(length = 300)
    private String motivoReembolso;
}

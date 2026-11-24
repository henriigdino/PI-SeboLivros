package com.sebo.livros.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "livros")
@Data
public class Livro {

    // Identificador unico gerado pelo banco H2.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, length = 150)
    private String autor;

    @Column(length = 20)
    private String isbn;

    @Column(length = 150)
    private String editora;

    private Integer ano;

    // Id da categoria; a existencia de uma categoria ativa e validada no LivroService.
    @Column(nullable = false)
    private Long categoriaId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    // Valores esperados: DISPONIVEL, RESERVADO e VENDIDO.
    @Column(nullable = false, length = 20)
    private String status = "DISPONIVEL";

    @Column(length = 2000)
    private String descricao;

    // A imagem fica armazenada em binario, fora das respostas JSON.
    @Lob
    @Column(name = "foto", columnDefinition = "BLOB")
    private byte[] foto;

    @Column(nullable = false)
    private Boolean ativo = true;
}

package com.sebo.livros.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "clientes")
@Data
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 10)
    private String tipoPessoa;

    @Column(nullable = false, unique = true, length = 14)
    private String documento;

    @Column(length = 20)
    private String rg;

    @Column(length = 20)
    private String inscricaoEstadual;

    @Column(length = 150)
    private String email;

    @Column(length = 11)
    private String telefone;

    @Column(length = 8)
    private String cep;

    @Column(length = 150)
    private String logradouro;

    @Column(length = 20)
    private String numero;

    @Column(length = 100)
    private String complemento;

    @Column(length = 100)
    private String bairro;

    @Column(length = 100)
    private String cidade;

    @Column(length = 2)
    private String uf;

    @Column(nullable = false)
    private Boolean ativo = true;
}

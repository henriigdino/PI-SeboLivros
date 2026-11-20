
package com.sebo.livros.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "fornecedores")
@Data
public class Fornecedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 18)
    private String documento;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String telefone;

    @Column(length = 150)
    private String endereco;

    @Column(nullable = false)
    private Boolean ativo = true;
}

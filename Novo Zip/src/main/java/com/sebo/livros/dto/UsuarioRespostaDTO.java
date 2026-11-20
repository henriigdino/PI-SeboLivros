package com.sebo.livros.dto;

import lombok.Data;

@Data
public class UsuarioRespostaDTO {

    private Long id;
    private String nome;
    private String email;
    private String tipo;
    private Boolean ativo;
}

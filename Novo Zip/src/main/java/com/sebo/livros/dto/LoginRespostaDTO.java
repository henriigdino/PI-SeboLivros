
package com.sebo.livros.dto;

import lombok.Data;

@Data
public class LoginRespostaDTO {

    private Long id;
    private String nome;
    private String email;
    private String tipo;

    public LoginRespostaDTO(Long id, String nome, String email, String tipo) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.tipo = tipo;
    }
}


package com.sebo.livros.dto;

import lombok.Data;

@Data
public class FornecedorRespostaDTO {

    private Long id;
    private String nome;
    private String documento;
    private String email;
    private String telefone;
    private String endereco;
    private Boolean ativo;
}

package com.sebo.livros.dto;

import lombok.Data;
import java.math.BigDecimal;

// Dados retornados pela API; a imagem nao e incluida no JSON.
@Data
public class LivroRespostaDTO {
    private Long id;
    private String titulo;
    private String autor;
    private String isbn;
    private String editora;
    private Integer ano;
    private Long categoriaId;
    private String categoriaNome;
    private BigDecimal preco;
    private String status;
    private String descricao;
    private Boolean ativo;
    private Boolean possuiHistoricoVendas;
}

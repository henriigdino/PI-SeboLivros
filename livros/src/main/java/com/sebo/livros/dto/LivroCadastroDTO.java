package com.sebo.livros.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

// Dados recebidos do Angular para cadastrar ou atualizar um livro.
@Data
public class LivroCadastroDTO {

    @NotBlank(message = "Titulo obrigatorio")
    @Size(max = 200, message = "Titulo deve ter ate 200 caracteres")
    private String titulo;

    @NotBlank(message = "Autor obrigatorio")
    @Size(max = 150, message = "Autor deve ter ate 150 caracteres")
    private String autor;

    @Pattern(regexp = "^$|^[0-9Xx\\- ]{8,20}$", message = "ISBN invalido")
    @Size(max = 20, message = "ISBN deve ter ate 20 caracteres")
    private String isbn;

    @Size(max = 150, message = "Editora deve ter ate 150 caracteres")
    private String editora;

    @Min(value = 1450, message = "Ano invalido")
    @Max(value = 2100, message = "Ano invalido")
    private Integer ano;

    @NotNull(message = "Categoria obrigatoria")
    private Long categoriaId;

    @NotNull(message = "Preco obrigatorio")
    @DecimalMin(value = "0.01", message = "Preco deve ser maior que zero")
    private BigDecimal preco;

    private String status;

    @Size(max = 2000, message = "Descricao deve ter ate 2000 caracteres")
    private String descricao;
}

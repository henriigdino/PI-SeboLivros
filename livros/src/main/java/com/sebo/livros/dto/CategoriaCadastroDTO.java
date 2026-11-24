package com.sebo.livros.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaCadastroDTO(
        @NotBlank(message = "Informe o nome da categoria")
        @Size(max = 80, message = "Nome deve ter ate 80 caracteres")
        String nome,

        @Size(max = 300, message = "Descricao deve ter ate 300 caracteres")
        String descricao) { }

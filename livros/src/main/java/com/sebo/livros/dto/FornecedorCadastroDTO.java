package com.sebo.livros.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FornecedorCadastroDTO(
        @NotBlank(message = "Nome obrigatorio")
        @Size(max = 150, message = "Nome deve ter ate 150 caracteres")
        String nome,

        @NotBlank(message = "Documento obrigatorio")
        @Size(max = 18, message = "Documento deve ter ate 18 caracteres")
        String documento,

        @Email(message = "E-mail invalido")
        @Size(max = 100, message = "E-mail deve ter ate 100 caracteres")
        String email,

        @Size(max = 11, message = "Telefone deve ter ate 11 caracteres")
        String telefone,

        @Size(max = 150, message = "Endereco deve ter ate 150 caracteres")
        String endereco) { }

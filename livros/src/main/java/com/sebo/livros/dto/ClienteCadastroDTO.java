package com.sebo.livros.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClienteCadastroDTO(
        @NotBlank(message = "Nome obrigatorio")
        @Size(max = 150, message = "Nome deve ter ate 150 caracteres")
        String nome,

        @NotBlank(message = "Tipo de pessoa obrigatorio")
        @Pattern(regexp = "(?i)^(FISICA|JURIDICA)$", message = "Tipo de pessoa invalido")
        String tipoPessoa,

        @NotBlank(message = "Documento obrigatorio")
        @Size(max = 20, message = "Documento deve ter ate 20 caracteres")
        String documento,

        @Size(max = 20, message = "RG deve ter ate 20 caracteres")
        String rg,

        @Size(max = 20, message = "Inscricao estadual deve ter ate 20 caracteres")
        String inscricaoEstadual,

        @Email(message = "E-mail invalido")
        @Size(max = 150, message = "E-mail deve ter ate 150 caracteres")
        String email,

        @Size(max = 11, message = "Telefone deve ter ate 11 caracteres")
        String telefone,

        @Size(max = 20, message = "CEP deve ter ate 20 caracteres")
        String cep,

        @Size(max = 150, message = "Logradouro deve ter ate 150 caracteres")
        String logradouro,

        @Size(max = 20, message = "Numero deve ter ate 20 caracteres")
        String numero,

        @Size(max = 100, message = "Complemento deve ter ate 100 caracteres")
        String complemento,

        @Size(max = 100, message = "Bairro deve ter ate 100 caracteres")
        String bairro,

        @Size(max = 100, message = "Cidade deve ter ate 100 caracteres")
        String cidade,

        @Pattern(regexp = "^$|^[A-Za-z]{2}$", message = "UF deve conter 2 letras")
        String uf) {
}

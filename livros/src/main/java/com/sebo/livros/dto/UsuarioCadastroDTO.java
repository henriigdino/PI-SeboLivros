package com.sebo.livros.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioCadastroDTO(
        @NotBlank(message = "Nome obrigatorio")
        @Size(max = 100, message = "Nome deve ter ate 100 caracteres")
        String nome,

        @NotBlank(message = "E-mail obrigatorio")
        @Email(message = "E-mail invalido")
        @Size(max = 100, message = "E-mail deve ter ate 100 caracteres")
        String email,

        @Size(min = 6, max = 100, message = "Senha deve ter entre 6 e 100 caracteres")
        String senha,

        @NotBlank(message = "Perfil obrigatorio")
        @Pattern(regexp = "(?i)^(ADMIN|FUNCIONARIO)$", message = "Perfil invalido")
        String tipo) { }

package com.sebo.livros.dto;

import jakarta.validation.constraints.Size;

public record ReembolsoDTO(
        @Size(max = 300, message = "Motivo deve ter ate 300 caracteres")
        String motivo) { }

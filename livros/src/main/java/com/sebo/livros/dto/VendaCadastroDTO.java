package com.sebo.livros.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VendaCadastroDTO(
        @NotNull(message = "Informe o cliente") Long clienteId,
        @NotNull(message = "Informe o livro") Long livroId,

        // O valor e a data sao opcionais, mas quando enviados precisam ser validos.
        @DecimalMin(value = "0.01", message = "Valor invalido") BigDecimal valor,
        LocalDateTime data,

        @NotBlank(message = "Informe a forma de pagamento")
        @Pattern(regexp = "(?i)^(DINHEIRO|PIX|CARTAO_CREDITO|CARTAO_DEBITO|BOLETO|TRANSFERENCIA)$",
                message = "Forma de pagamento invalida")
        String formaPagamento) { }

package com.sebo.livros.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VendaRespostaDTO(
        Long id, LocalDateTime data,
        Long clienteId, String clienteNome,
        Long livroId, String livroTitulo,
        BigDecimal valor, String formaPagamento, String status,
        LocalDateTime dataReembolso, String motivoReembolso) { }

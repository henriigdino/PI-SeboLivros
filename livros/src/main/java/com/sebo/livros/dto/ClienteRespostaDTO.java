package com.sebo.livros.dto;

public record ClienteRespostaDTO(
        Long id,
        String nome,
        String tipoPessoa,
        String documento,
        String rg,
        String inscricaoEstadual,
        String email,
        String telefone,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf,
        Boolean ativo) {
}

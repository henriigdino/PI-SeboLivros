package com.sebo.livros.util;

/**
 * Validacao de CPF/CNPJ compartilhada por Clientes e Fornecedores.
 *
 * <p>A validacao considera apenas a quantidade de digitos: 11 para CPF e 14 para
 * CNPJ. Digitos verificadores e sequencias repetidas nao sao exigidos, para
 * aceitar documentos de teste como {@code 11.1111-11/111111}.</p>
 */
public final class Documentos {

    private Documentos() { }

    public static String digitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    public static boolean documentoValido(String valor) {
        int tamanho = digitos(valor).length();
        return tamanho == 11 || tamanho == 14;
    }

    public static boolean cpfValido(String valor) {
        return digitos(valor).length() == 11;
    }

    public static boolean cnpjValido(String valor) {
        return digitos(valor).length() == 14;
    }
}

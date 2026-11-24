package com.sebo.livros.util;

/**
 * Validacao de CPF/CNPJ compartilhada por Clientes e Fornecedores.
 *
 * <p>Valida a quantidade de digitos, os digitos verificadores e rejeita
 * sequencias repetidas.</p>
 */
public final class Documentos {

    private static final int[] PESOS_CPF_PRIMEIRO = {10, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_CPF_SEGUNDO = {11, 10, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_CNPJ_PRIMEIRO = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_CNPJ_SEGUNDO = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    private Documentos() { }

    public static String digitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    public static boolean documentoValido(String valor) {
        String d = digitos(valor);
        if (d.length() == 11) {
            return cpfValido(d);
        }
        if (d.length() == 14) {
            return cnpjValido(d);
        }
        return false;
    }

    public static boolean cpfValido(String valor) {
        String d = digitos(valor);
        if (d.length() != 11 || sequenciaRepetida(d)) {
            return false;
        }
        int[] numeros = numeros(d);
        return calcularDigito(numeros, PESOS_CPF_PRIMEIRO) == numeros[9]
                && calcularDigito(numeros, PESOS_CPF_SEGUNDO) == numeros[10];
    }

    public static boolean cnpjValido(String valor) {
        String d = digitos(valor);
        if (d.length() != 14 || sequenciaRepetida(d)) {
            return false;
        }
        int[] numeros = numeros(d);
        return calcularDigito(numeros, PESOS_CNPJ_PRIMEIRO) == numeros[12]
                && calcularDigito(numeros, PESOS_CNPJ_SEGUNDO) == numeros[13];
    }

    private static int[] numeros(String d) {
        int[] numeros = new int[d.length()];
        for (int i = 0; i < d.length(); i++) {
            numeros[i] = d.charAt(i) - '0';
        }
        return numeros;
    }

    private static int calcularDigito(int[] numeros, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += numeros[i] * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static boolean sequenciaRepetida(String d) {
        char primeiro = d.charAt(0);
        for (int i = 1; i < d.length(); i++) {
            if (d.charAt(i) != primeiro) {
                return false;
            }
        }
        return true;
    }
}

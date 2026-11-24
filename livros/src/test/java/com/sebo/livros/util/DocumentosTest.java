package com.sebo.livros.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentosTest {

    @Test
    void digitosRemoveMascara() {
        assertEquals("52998224725", Documentos.digitos("529.982.247-25"));
        assertEquals("", Documentos.digitos(null));
    }

    @Test
    void cpfValidoAceitaMascaraERejeitaDigitosVerificadoresErrados() {
        assertTrue(Documentos.cpfValido("529.982.247-25"));
        assertTrue(Documentos.cpfValido("12345678909"));
        assertFalse(Documentos.cpfValido("52998224726"));
        assertFalse(Documentos.cpfValido("11111111111"));
        assertFalse(Documentos.cpfValido("123"));
    }

    @Test
    void cnpjValidoAceitaMascaraERejeitaDigitosVerificadoresErrados() {
        assertTrue(Documentos.cnpjValido("11.222.333/0001-81"));
        assertTrue(Documentos.cnpjValido("11444777000161"));
        assertFalse(Documentos.cnpjValido("11222333000182"));
        assertFalse(Documentos.cnpjValido("11111111111111"));
        assertFalse(Documentos.cnpjValido("123"));
    }

    @Test
    void documentoValidoEscolheCpfOuCnpjPelaQuantidadeDeDigitos() {
        assertTrue(Documentos.documentoValido("529.982.247-25"));
        assertTrue(Documentos.documentoValido("11.222.333/0001-81"));
        assertFalse(Documentos.documentoValido("11111111111"));
        assertFalse(Documentos.documentoValido("11111111111111"));
        assertFalse(Documentos.documentoValido("123"));
    }
}

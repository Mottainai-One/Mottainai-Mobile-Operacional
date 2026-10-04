package com.mottainai.operacional.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AppLocalesTest {

    @Test
    public void ptBrUsesBrazilianPortugueseLanguageTag() {
        assertEquals("pt-BR", AppLocales.PT_BR.toLanguageTag());
    }
}

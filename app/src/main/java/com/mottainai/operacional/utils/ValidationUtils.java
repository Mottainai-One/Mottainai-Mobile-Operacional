package com.mottainai.operacional.utils;

import android.util.Patterns;

/** Validações de formato compartilhadas entre os formulários do app. */
public final class ValidationUtils {

    private ValidationUtils() {}

    public static boolean isValidEmail(String value) {
        return value != null && !value.trim().isEmpty()
                && Patterns.EMAIL_ADDRESS.matcher(value.trim()).matches();
    }

    /** Valida os 14 dígitos de um CNPJ, incluindo os dois dígitos verificadores (módulo 11). */
    public static boolean isValidCnpj(String digitsOnly) {
        if (digitsOnly == null || digitsOnly.length() != 14 || !digitsOnly.matches("\\d{14}")) {
            return false;
        }
        // CNPJs com todos os dígitos iguais passam no cálculo do módulo 11
        // mas nunca são documentos reais emitidos.
        if (digitsOnly.chars().distinct().count() == 1) {
            return false;
        }
        int[] digits = new int[14];
        for (int i = 0; i < 14; i++) digits[i] = digitsOnly.charAt(i) - '0';

        int firstCheck = checkDigit(digits, 12, new int[]{5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2});
        if (firstCheck != digits[12]) return false;

        int secondCheck = checkDigit(digits, 13, new int[]{6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2});
        return secondCheck == digits[13];
    }

    private static int checkDigit(int[] digits, int length, int[] weights) {
        int sum = 0;
        for (int i = 0; i < length; i++) sum += digits[i] * weights[i];
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}

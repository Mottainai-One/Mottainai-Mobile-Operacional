package com.mottainai.operacional.utils;

import java.util.Locale;

public class RoleHelper {

    // Dono é o único que pode ver a aba Config
    public static boolean isOwner(String role) {
        return Constants.ROLE_DONO.equals(normalize(role));
    }

    // Gerente e Dono podem cadastrar produto
    public static boolean canRegisterProduct(String role) {
        String normalizedRole = normalize(role);
        return Constants.ROLE_GERENTE.equals(normalizedRole)
                || Constants.ROLE_DONO.equals(normalizedRole);
    }

    // Estoquista NÃO pode ver sugestões
    public static boolean canViewSuggestions(String role) {
        return role == null || !Constants.ROLE_ESTOQUISTA.equals(normalize(role));
    }

    // Traduz o role em nome de exibição (vem do switch que está no Fragment)
    public static String roleToLabel(String role) {
        if (role == null) return "—";
        switch (normalize(role)) {
            case Constants.ROLE_ESTOQUISTA: return "Estoquista";
            case Constants.ROLE_GERENTE:    return "Gerente";
            case Constants.ROLE_DONO:       return "Dono";
            default: return role;
        }
    }

    /**
     * Mantém a UI compatível tanto com os perfis em português salvos no Firebase
     * quanto com os aliases em inglês emitidos por integrações anteriores.
     */
    private static String normalize(String role) {
        if (role == null) return "";

        switch (role.trim().toUpperCase(Locale.ROOT)) {
            case "ADMIN":
            case "OWNER":
                return Constants.ROLE_DONO;
            case "MANAGER":
                return Constants.ROLE_GERENTE;
            case "STOCK":
                return Constants.ROLE_ESTOQUISTA;
            default:
                return role.trim().toLowerCase(Locale.ROOT);
        }
    }
}

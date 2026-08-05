package com.mottainai.operacional.utils;

public class RoleHelper {

    // Dono é o único que pode ver a aba Config
    public static boolean isOwner(String role) {
        return Constants.ROLE_DONO.equals(role);
    }

    // Gerente e Dono podem cadastrar produto
    public static boolean canRegisterProduct(String role) {
        return Constants.ROLE_GERENTE.equals(role)
                || Constants.ROLE_DONO.equals(role);
    }

    // Estoquista NÃO pode ver sugestões
    public static boolean canViewSuggestions(String role) {
        return !Constants.ROLE_ESTOQUISTA.equals(role);
    }

    // Traduz o role em nome de exibição (vem do switch que está no Fragment)
    public static String roleToLabel(String role) {
        if (role == null) return "—";
        switch (role) {
            case Constants.ROLE_ESTOQUISTA: return "Estoquista";
            case Constants.ROLE_GERENTE:    return "Gerente";
            case Constants.ROLE_DONO:       return "Dono";
            default: return role;
        }
    }
}

package com.mottainai.operacional.utils;

public class RoleHelper {

    // Dono é o único que pode ver a aba Config
    public static boolean isOwner(String role) {
        return role != null && Constants.ROLE_DONO.equalsIgnoreCase(role.trim());
    }

    // Gerente e Dono podem cadastrar produto
    public static boolean canRegisterProduct(String role) {
        if (role == null) return false;
        String r = role.trim();
        return Constants.ROLE_GERENTE.equalsIgnoreCase(r)
                || Constants.ROLE_DONO.equalsIgnoreCase(r);
    }

    // Estoquista NÃO pode ver sugestões
    public static boolean canViewSuggestions(String role) {
        return role == null || !Constants.ROLE_ESTOQUISTA.equalsIgnoreCase(role.trim());
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

package com.mottainai.operacional.utils;

public class RoleHelper {

    public static boolean isOwner(String role) {
        return Constants.ROLE_DONO.equals(role);
    }

    public static boolean canRegisterProduct(String role) {
        return Constants.ROLE_GERENTE.equals(role)
                || Constants.ROLE_DONO.equals(role);
    }

    public static boolean canViewSuggestions(String role) {
        return !Constants.ROLE_ESTOQUISTA.equals(role);
    }

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
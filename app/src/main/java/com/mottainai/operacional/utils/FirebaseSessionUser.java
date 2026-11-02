package com.mottainai.operacional.utils;

import com.mottainai.operacional.models.User;

import java.util.Map;

/** Builds the operational session from Firebase's verified custom claims. */
public final class FirebaseSessionUser {

    private FirebaseSessionUser() {}

    public static User fromClaims(String uid, String email, Map<String, Object> claims) {
        if (isBlank(uid) || claims == null) return null;

        String role = RoleHelper.normalize(asString(claims.get("role")));
        if (!Constants.ROLE_ESTOQUISTA.equals(role)
                && !Constants.ROLE_GERENTE.equals(role)
                && !Constants.ROLE_DONO.equals(role)) {
            return null;
        }

        String storeId = asString(claims.get("storeID"));
        if (isBlank(storeId)) storeId = asString(claims.get("storeId"));
        if (isBlank(storeId)) return null;

        String name = asString(claims.get("name"));
        if (isBlank(name)) name = emailName(email);

        return new User(uid, name, role, storeId);
    }

    private static String emailName(String email) {
        if (isBlank(email)) return "Usuário";
        int separator = email.indexOf('@');
        return separator > 0 ? email.substring(0, separator) : email;
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

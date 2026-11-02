package com.mottainai.operacional.utils;

import com.mottainai.operacional.models.ApiUserProfile;
import com.mottainai.operacional.models.User;

/** Firebase provides identity only; the API provides the operational assignment. */
public final class OperationalSessionUser {
    private OperationalSessionUser() {}

    public static User fromProfile(String authenticatedUid, ApiUserProfile profile) {
        if (authenticatedUid == null || authenticatedUid.trim().isEmpty() || profile == null
                || !authenticatedUid.equals(profile.getFirebaseUid()) || !Boolean.TRUE.equals(profile.getActive())
                || profile.getStoreId() == null || profile.getStoreId() <= 0
                || profile.getCompanyId() == null || profile.getCompanyId() <= 0
                || !RoleHelper.isKnownRole(profile.getRole())) return null;
        String name = profile.getName();
        return new User(authenticatedUid, name == null || name.trim().isEmpty() ? "Usuário" : name.trim(),
                RoleHelper.normalize(profile.getRole()), String.valueOf(profile.getStoreId()));
    }
}

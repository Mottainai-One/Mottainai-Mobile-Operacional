package com.mottainai.operacional.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.mottainai.operacional.models.User;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class FirebaseSessionUserTest {

    @Test
    public void buildsOperationalSessionFromVerifiedFirebaseClaims() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "GERENTE");
        claims.put("storeID", "store-qa");

        User user = FirebaseSessionUser.fromClaims("firebase-uid", "gerente@mottainai.com", claims);

        assertEquals("firebase-uid", user.getUid());
        assertEquals("gerente", user.getRole());
        assertEquals("store-qa", user.getStoreId());
        assertEquals("gerente", user.getName());
    }

    @Test
    public void rejectsClaimsWithoutStoreScope() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ESTOQUISTA");

        assertNull(FirebaseSessionUser.fromClaims("firebase-uid", "estoquista@mottainai.com", claims));
    }
}

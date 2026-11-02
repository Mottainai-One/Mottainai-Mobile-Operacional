package com.mottainai.operacional.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.mottainai.operacional.models.ApiLoginRequest;
import com.mottainai.operacional.models.ApiTokenResponse;
import com.mottainai.operacional.models.ApiUserProfile;
import com.mottainai.operacional.models.ProductCreateRequest;
import com.mottainai.operacional.models.ProductUpdateRequest;
import java.math.BigDecimal;
import org.junit.Test;

public class OperationalContractTest {
    private final Gson gson = new Gson();

    @Test public void loginExchangeUsesFirebaseTokenAndStaffPasswordContract() {
        String json = gson.toJson(new ApiLoginRequest("firebase-id-token", "staff-password"));
        assertTrue(json.contains("\"firebaseIdToken\":\"firebase-id-token\""));
        assertTrue(json.contains("\"password\":\"staff-password\""));
    }

    @Test public void tokenResponseKeepsApiAccessAndRefreshTokensSeparate() {
        ApiTokenResponse response = gson.fromJson("{\"accessToken\":\"api-access\",\"refreshToken\":\"api-refresh\",\"expiresIn\":60,\"refreshExpiresIn\":604800}", ApiTokenResponse.class);
        assertEquals("api-access", response.getAccessToken());
        assertEquals("api-refresh", response.getRefreshToken());
        assertEquals(60, response.getExpiresIn());
    }

    @Test public void profileMapsNumericStoreAndActiveRole() {
        ApiUserProfile profile = gson.fromJson("{\"id\":7,\"name\":\"QA\",\"role\":\"Manager\",\"active\":true,\"storeId\":3}", ApiUserProfile.class);
        assertEquals(Integer.valueOf(3), profile.getStoreId());
        assertEquals("Manager", profile.getRole());
        assertEquals(Boolean.TRUE, profile.getActive());
    }

    @Test public void productCreateAndUpdateMatchDifferentBackendContracts() {
        String create = gson.toJson(new ProductCreateRequest(2, 5, "7891234567890", "12345678", null,
                "Arroz", null, null, "KG", BigDecimal.ONE,
                "https://res.cloudinary.com/demo/image/upload/v1/rice.jpg"));
        assertTrue(create.contains("\"taxProfileId\":5"));
        assertTrue(create.contains("\"barcode\":\"7891234567890\""));
        String update = gson.toJson(new ProductUpdateRequest(2, 5, "12345678", null, "Arroz", null, null,
                "KG", BigDecimal.ONE, true, 4, null));
        assertTrue(update.contains("\"active\":true"));
        assertTrue(update.contains("\"version\":4"));
        assertTrue(!update.contains("barcode"));
    }
}

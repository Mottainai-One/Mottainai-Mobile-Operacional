package com.mottainai.operacional;

import com.google.gson.Gson;
import com.mottainai.operacional.models.ApiUserProfile;
import com.mottainai.operacional.models.User;
import com.mottainai.operacional.utils.OperationalSessionUser;
import org.junit.Test;
import static org.junit.Assert.*;

public class OperationalSessionUserTest {
    private final Gson gson = new Gson();
    private ApiUserProfile profile(String extra) {
        return gson.fromJson("{\"firebaseUid\":\"verified-uid\",\"name\":\"Pessoa\",\"role\":\"gerente\","
                + "\"active\":true,\"storeId\":5,\"companyId\":3" + extra + "}", ApiUserProfile.class);
    }

    @Test public void usesDatabaseProfileWithoutFirebaseCustomClaims() {
        User user = OperationalSessionUser.fromProfile("verified-uid", profile(""));
        assertNotNull(user);
        assertEquals("gerente", user.getRole());
        assertEquals("5", user.getStoreId());
        assertEquals("Pessoa", user.getName());
    }

    @Test public void refusesProfileOfAnotherIdentity() {
        assertNull(OperationalSessionUser.fromProfile("another-uid", profile("")));
    }

    @Test public void refusesInactiveUnknownRoleAndMissingTenant() {
        for (String json : new String[] {
                "{\"firebaseUid\":\"verified-uid\",\"role\":\"gerente\",\"active\":false,\"storeId\":5,\"companyId\":3}",
                "{\"firebaseUid\":\"verified-uid\",\"role\":\"unknown\",\"active\":true,\"storeId\":5,\"companyId\":3}",
                "{\"firebaseUid\":\"verified-uid\",\"role\":\"gerente\",\"active\":true,\"storeId\":5}",
                "{\"firebaseUid\":\"verified-uid\",\"role\":\"gerente\",\"active\":true,\"storeId\":0,\"companyId\":3}" }) {
            assertNull(OperationalSessionUser.fromProfile("verified-uid", gson.fromJson(json, ApiUserProfile.class)));
        }
    }
}

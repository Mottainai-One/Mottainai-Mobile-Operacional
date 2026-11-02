package com.mottainai.operacional;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.mottainai.operacional.models.ApiLoginRequest;
import org.junit.Test;
import static org.junit.Assert.*;

public class ApiLoginPayloadTest {
    @Test public void apiReceivesTokenAndNeverFirebasePassword() {
        var json = JsonParser.parseString(new Gson().toJson(new ApiLoginRequest("id-token"))).getAsJsonObject();
        assertEquals(1, json.size());
        assertEquals("id-token", json.get("firebaseIdToken").getAsString());
        assertFalse(json.has("password"));
    }
}

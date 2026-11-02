package com.mottainai.operacional.models;

import com.google.gson.annotations.SerializedName;

public class ApiLoginRequest {
    @SerializedName("firebaseIdToken") private final String firebaseIdToken;
    public ApiLoginRequest(String firebaseIdToken) {
        this.firebaseIdToken = firebaseIdToken;
    }
}

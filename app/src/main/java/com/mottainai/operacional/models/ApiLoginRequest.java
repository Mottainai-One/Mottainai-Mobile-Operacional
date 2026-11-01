package com.mottainai.operacional.models;

import com.google.gson.annotations.SerializedName;

public class ApiLoginRequest {
    @SerializedName("firebaseIdToken") private final String firebaseIdToken;
    @SerializedName("password") private final String password;
    public ApiLoginRequest(String firebaseIdToken, String password) {
        this.firebaseIdToken = firebaseIdToken;
        this.password = password;
    }
}

package com.mottainai.operacional.models;

import com.google.gson.annotations.SerializedName;

public class ApiRefreshRequest {
    @SerializedName("refreshToken") private final String refreshToken;
    public ApiRefreshRequest(String refreshToken) { this.refreshToken = refreshToken; }
}

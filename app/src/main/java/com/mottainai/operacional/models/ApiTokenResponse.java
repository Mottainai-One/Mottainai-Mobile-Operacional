package com.mottainai.operacional.models;

import com.google.gson.annotations.SerializedName;

public class ApiTokenResponse {
    @SerializedName("accessToken") private String accessToken;
    @SerializedName("refreshToken") private String refreshToken;
    @SerializedName("expiresIn") private long expiresIn;
    @SerializedName("refreshExpiresIn") private long refreshExpiresIn;
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public long getExpiresIn() { return expiresIn; }
    public long getRefreshExpiresIn() { return refreshExpiresIn; }
}

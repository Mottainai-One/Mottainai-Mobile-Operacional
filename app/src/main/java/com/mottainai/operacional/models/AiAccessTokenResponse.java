package com.mottainai.operacional.models;

import com.google.gson.annotations.SerializedName;

/** Resposta da API relacional ao autorizar o acesso temporário à Mottainai-IA. */
public class AiAccessTokenResponse {

    @SerializedName("accessToken")
    private String accessToken;

    @SerializedName("tokenType")
    private String tokenType;

    @SerializedName("expiresIn")
    private long expiresIn;

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }
}

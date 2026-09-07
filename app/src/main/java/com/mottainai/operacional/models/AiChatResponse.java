package com.mottainai.operacional.models;

import com.google.gson.annotations.SerializedName;

/** Parte da resposta da IA exibida ao usuário. */
public class AiChatResponse {

    @SerializedName("session_id")
    private String sessionId;

    private String response;

    public String getSessionId() {
        return sessionId;
    }

    public String getResponse() {
        return response;
    }
}

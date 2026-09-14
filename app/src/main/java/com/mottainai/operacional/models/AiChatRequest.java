package com.mottainai.operacional.models;

import com.google.gson.annotations.SerializedName;

/** Corpo aceito pelo endpoint POST /chat da Mottainai-IA. */
public class AiChatRequest {

    private final String message;

    @SerializedName("session_id")
    private final String sessionId;

    public AiChatRequest(String message, String sessionId) {
        this.message = message;
        this.sessionId = sessionId;
    }
}

package com.mottainai.operacional.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/** Resposta do endpoint GET /chat/history/{session_id}. */
public class AiChatHistoryResponse {

    @SerializedName("session_id")
    private String sessionId;

    private List<AiChatHistoryMessage> messages;

    public String getSessionId() {
        return sessionId;
    }

    public List<AiChatHistoryMessage> getMessages() {
        return messages;
    }
}

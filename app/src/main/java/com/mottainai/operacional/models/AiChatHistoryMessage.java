package com.mottainai.operacional.models;

/** Mensagem persistida por uma sessão de chat da Mottainai-IA. */
public class AiChatHistoryMessage {

    private String role;
    private String content;

    public String getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }
}

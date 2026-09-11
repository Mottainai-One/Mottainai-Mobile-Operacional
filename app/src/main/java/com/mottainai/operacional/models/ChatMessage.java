package com.mottainai.operacional.models;

/** Estado visual de uma mensagem no chat. Não é uma entidade persistida localmente. */
public class ChatMessage {

    private final String content;
    private final boolean sentByCurrentUser;

    public ChatMessage(String content, boolean sentByCurrentUser) {
        this.content = content;
        this.sentByCurrentUser = sentByCurrentUser;
    }

    public String getContent() {
        return content;
    }

    public boolean isSentByCurrentUser() {
        return sentByCurrentUser;
    }
}

package com.mottainai.operacional.viewmodels;

import androidx.lifecycle.ViewModel;
import com.mottainai.operacional.models.ChatMessage;
import java.util.ArrayList;
import java.util.List;

/** In-memory rotation state, never shared with a different identity or store. */
public class ChatScreenState extends ViewModel {
    public String owner;
    public String draft = "";
    public String pending;
    public final List<ChatMessage> messages = new ArrayList<>();
}

package com.mottainai.operacional.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.mottainai.operacional.R;
import com.mottainai.operacional.databinding.FragmentChatAiBinding;
import com.mottainai.operacional.adapters.ChatMessageAdapter;
import com.mottainai.operacional.models.AiChatHistoryMessage;
import com.mottainai.operacional.models.AiChatHistoryResponse;
import com.mottainai.operacional.models.AiChatRequest;
import com.mottainai.operacional.models.AiChatResponse;
import com.mottainai.operacional.models.ChatMessage;
import com.mottainai.operacional.network.AiApiService;
import com.mottainai.operacional.network.AiRetrofitClient;
import com.mottainai.operacional.network.RetrofitClient;
import com.mottainai.operacional.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Conversa operacional com a Mottainai-IA.
 *
 * O app envia à IA o access token operacional emitido pela API SQL. A IA
 * verifica a assinatura e vincula o UID Firebase ao contexto RLS do banco.
 */
public class ChatAiFragment extends Fragment {

    private FragmentChatAiBinding binding;

    private enum RetryAction { NONE, SEND, HISTORY }

    private final ChatMessageAdapter messageAdapter = new ChatMessageAdapter();

    private RecyclerView messagesView;
    private View emptyChatState;
    private TextInputEditText messageInput;
    private MaterialButton sendButton;
    private MaterialButton retryButton;
    private ProgressBar progressBar;
    private TextView statusView;
    private TextView errorView;
    private SessionManager sessionManager;

    private String sessionId;
    private String pendingMessage;
    private boolean requestInProgress;
    private RetryAction retryAction = RetryAction.NONE;
    private int composerBaseBottomMargin;
    private com.mottainai.operacional.viewmodels.ChatScreenState screenState;
    private int viewGeneration;
    private String ownerKey;

    private String currentOwner() {
        return sessionManager.getUid() + ":" + sessionManager.getStoreId() + ":"
                + com.mottainai.operacional.BuildConfig.IA_BASE_URL;
    }

    private boolean acceptsCallback(int generation) {
        return isAdded() && binding != null && viewGeneration == generation && ownerKey.equals(currentOwner());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChatAiBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        viewGeneration++;
        ownerKey = currentOwner();
        screenState = new androidx.lifecycle.ViewModelProvider(this).get(
                com.mottainai.operacional.viewmodels.ChatScreenState.class);
        if (!ownerKey.equals(screenState.owner)) {
            screenState.messages.clear();
            screenState.draft = "";
            screenState.pending = null;
            screenState.owner = ownerKey;
        }
        messageAdapter.replaceMessages(screenState.messages);
        pendingMessage = screenState.pending;
        requestInProgress = false;
        sessionId = sessionManager.getAiChatSessionId();
        boolean hasSavedSession = sessionId != null && !sessionId.trim().isEmpty();
        if (!hasSavedSession) {
            sessionId = UUID.randomUUID().toString();
        }

        messagesView = binding.rvChatMessages;
        emptyChatState = binding.emptyChatState;
        messageInput = binding.etChatMessage;
        messageInput.setText(screenState.draft);
        sendButton = binding.btnChatSend;
        retryButton = binding.btnChatRetry;
        progressBar = binding.progressChat;
        statusView = binding.tvChatStatus;
        errorView = binding.tvChatError;
        View composer = binding.chatComposer;
        configureComposerInsets(view, composer);

        messagesView.setLayoutManager(new LinearLayoutManager(requireContext()));
        messagesView.setAdapter(messageAdapter);
        renderConversationState();

        sendButton.setOnClickListener(v -> sendFromInput());
        retryButton.setOnClickListener(v -> retryLastRequest());
        messageInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendFromInput();
                return true;
            }
            return false;
        });

        if (hasSavedSession) {
            loadHistory();
        }
    }

    private void sendFromInput() {
        String message = messageInput.getText() == null ? "" : messageInput.getText().toString().trim();
        if (message.isEmpty()) {
            messageInput.setError("Digite uma pergunta para a IA");
            messageInput.requestFocus();
            return;
        }
        sendMessage(message, true);
    }

    private void sendMessage(String message, boolean appendUserMessage) {
        if (!ownerKey.equals(currentOwner())) return;
        final int generation = viewGeneration;
        if (requestInProgress) {
            Toast.makeText(requireContext(), "Aguarde a resposta atual.", Toast.LENGTH_SHORT).show();
            return;
        }

        pendingMessage = message;
        sessionManager.saveAiChatSessionId(sessionId);
        if (appendUserMessage) {
            messageAdapter.addMessage(new ChatMessage(message, true));
            messageInput.setText("");
            renderConversationState();
            scrollToLatestMessage();
        }

        showLoading(getString(R.string.chat_sending));
        requestAiAccessToken(RetryAction.SEND, accessToken -> {
            AiApiService aiService = AiRetrofitClient.getClient().create(AiApiService.class);
            aiService.sendMessage("Bearer " + accessToken, new AiChatRequest(pendingMessage, sessionId))
                    .enqueue(new Callback<AiChatResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<AiChatResponse> call,
                                               @NonNull Response<AiChatResponse> response) {
                            if (!acceptsCallback(generation)) return;
                            AiChatResponse body = response.body();
                            if (!response.isSuccessful() || body == null
                                    || body.getResponse() == null || body.getResponse().trim().isEmpty()) {
                                showRequestError(httpMessage("Não foi possível obter a resposta da IA.", response),
                                        RetryAction.SEND);
                                return;
                            }

                            if (body.getSessionId() != null && !body.getSessionId().trim().isEmpty()) {
                                sessionId = body.getSessionId();
                            }
                            sessionManager.saveAiChatSessionId(sessionId);
                            messageAdapter.addMessage(new ChatMessage(body.getResponse(), false));
                            pendingMessage = null;
                            renderConversationState();
                            finishRequest();
                            scrollToLatestMessage();
                        }

                        @Override
                        public void onFailure(@NonNull Call<AiChatResponse> call, @NonNull Throwable throwable) {
                            if (!acceptsCallback(generation)) return;
                            showRequestError("Não foi possível conectar à IA. Verifique a conexão e tente novamente.",
                                    RetryAction.SEND);
                        }
                    });
        });
    }

    private void loadHistory() {
        final int generation = viewGeneration;
        showLoading(getString(R.string.chat_loading_history));
        requestAiAccessToken(RetryAction.HISTORY, accessToken -> {
            AiApiService aiService = AiRetrofitClient.getClient().create(AiApiService.class);
            aiService.getHistory("Bearer " + accessToken, sessionId)
                    .enqueue(new Callback<AiChatHistoryResponse>() {
                @Override
                public void onResponse(@NonNull Call<AiChatHistoryResponse> call,
                                       @NonNull Response<AiChatHistoryResponse> response) {
                    if (!acceptsCallback(generation)) return;
                    AiChatHistoryResponse body = response.body();
                    if (!response.isSuccessful() || body == null) {
                        showRequestError(httpMessage("Não foi possível carregar a conversa anterior.", response),
                                RetryAction.HISTORY);
                        return;
                    }
                    messageAdapter.replaceMessages(toChatMessages(body.getMessages()));
                    renderConversationState();
                    finishRequest();
                    scrollToLatestMessage();
                }

                @Override
                public void onFailure(@NonNull Call<AiChatHistoryResponse> call, @NonNull Throwable throwable) {
                    if (!acceptsCallback(generation)) return;
                    showRequestError("Não foi possível conectar para carregar a conversa anterior.",
                            RetryAction.HISTORY);
                }
            });
        });
    }

    private List<ChatMessage> toChatMessages(List<AiChatHistoryMessage> history) {
        List<ChatMessage> messages = new ArrayList<>();
        if (history == null) return messages;

        for (AiChatHistoryMessage item : history) {
            if (item == null || item.getContent() == null || item.getContent().trim().isEmpty()) continue;
            messages.add(new ChatMessage(item.getContent(), "user".equalsIgnoreCase(item.getRole())));
        }
        return messages;
    }

    private void requestAiAccessToken(RetryAction errorAction, AiTokenCallback callback) {
        final int generation = viewGeneration;
        if (!sessionManager.hasCompleteProfile() || com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) {
            showRequestError("Sua sessão operacional expirou. Entre novamente para conversar com a IA.",
                    errorAction);
            return;
        }

        android.content.Context appContext = requireContext().getApplicationContext();
        androidx.fragment.app.FragmentActivity activity = requireActivity();
        new Thread(() -> {
            String accessToken = RetrofitClient.refreshApiAccessToken(appContext);
            activity.runOnUiThread(() -> {
                if (!acceptsCallback(generation)) return;
                if (accessToken == null || accessToken.trim().isEmpty()) {
                    showRequestError("Não foi possível renovar sua sessão operacional. Tente entrar novamente.",
                            errorAction);
                    return;
                }
                callback.onToken(accessToken);
            });
        }, "ai-api-token-refresh").start();
    }

    private void retryLastRequest() {
        if (requestInProgress) return;
        if (retryAction == RetryAction.HISTORY) {
            loadHistory();
        } else if (retryAction == RetryAction.SEND && pendingMessage != null && !pendingMessage.trim().isEmpty()) {
            sendMessage(pendingMessage, false);
        }
    }

    private void showLoading(String status) {
        requestInProgress = true;
        retryAction = RetryAction.NONE;
        errorView.setVisibility(View.GONE);
        retryButton.setVisibility(View.GONE);
        statusView.setText(status);
        statusView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        sendButton.setEnabled(false);
    }

    private void finishRequest() {
        requestInProgress = false;
        retryAction = RetryAction.NONE;
        progressBar.setVisibility(View.GONE);
        statusView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        retryButton.setVisibility(View.GONE);
        sendButton.setEnabled(true);
    }

    private void showRequestError(String message, RetryAction action) {
        requestInProgress = false;
        retryAction = action;
        progressBar.setVisibility(View.GONE);
        statusView.setVisibility(View.GONE);
        errorView.setText(message);
        errorView.setVisibility(View.VISIBLE);
        retryButton.setVisibility(View.VISIBLE);
        sendButton.setEnabled(true);
    }

    private String httpMessage(String defaultMessage, Response<?> response) {
        if (response.code() == 401) {
            return "Sua sessão não pôde ser validada. Entre novamente e tente de novo.";
        }
        if (response.code() == 403) {
            return "Seu perfil não tem permissão para usar a IA.";
        }
        return defaultMessage + " (código " + response.code() + ")";
    }

    private void scrollToLatestMessage() {
        if (messageAdapter.getItemCount() > 0) {
            final int generation = viewGeneration;
            messagesView.post(() -> {
                if (acceptsCallback(generation)) messagesView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
            });
        }
    }

    private void renderConversationState() {
        boolean hasMessages = messageAdapter.getItemCount() > 0;
        emptyChatState.setVisibility(hasMessages ? View.GONE : View.VISIBLE);
        messagesView.setVisibility(hasMessages ? View.VISIBLE : View.INVISIBLE);
    }

    private void configureComposerInsets(View root, View composer) {
        composerBaseBottomMargin = getResources()
                .getDimensionPixelSize(R.dimen.chat_composer_scanner_clearance);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            boolean keyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
            int desiredBottomMargin = keyboardVisible
                    ? Math.max(0, imeInsets.bottom - getBottomSpaceOutsideChat(root))
                            + getResources().getDimensionPixelSize(R.dimen.chat_composer_keyboard_gap)
                    : composerBaseBottomMargin;

            ViewGroup.LayoutParams layoutParams = composer.getLayoutParams();
            if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams margins = (ViewGroup.MarginLayoutParams) layoutParams;
                if (margins.bottomMargin != desiredBottomMargin) {
                    margins.bottomMargin = desiredBottomMargin;
                    composer.setLayoutParams(margins);
                }
            }
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    private int getBottomSpaceOutsideChat(View root) {
        int[] location = new int[2];
        root.getLocationInWindow(location);
        return Math.max(0, root.getRootView().getHeight() - location[1] - root.getHeight());
    }

    private interface AiTokenCallback {
        void onToken(String accessToken);
    }

    @Override
    public void onDestroyView() {
        if (screenState != null && ownerKey.equals(currentOwner())) {
            screenState.messages.clear();
            screenState.messages.addAll(messageAdapter.snapshot());
            screenState.pending = pendingMessage;
            screenState.draft = messageInput == null || messageInput.getText() == null
                    ? "" : messageInput.getText().toString();
        }
        viewGeneration++;
        requestInProgress = false;
        super.onDestroyView();
        binding = null;
        messagesView = null;
        emptyChatState = null;
        messageInput = null;
        sendButton = null;
        retryButton = null;
        progressBar = null;
        statusView = null;
        errorView = null;
    }
}

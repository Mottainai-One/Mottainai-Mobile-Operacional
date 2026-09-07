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
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.mottainai.operacional.R;
import com.mottainai.operacional.adapters.ChatMessageAdapter;
import com.mottainai.operacional.models.AiAccessTokenResponse;
import com.mottainai.operacional.models.AiChatHistoryMessage;
import com.mottainai.operacional.models.AiChatHistoryResponse;
import com.mottainai.operacional.models.AiChatRequest;
import com.mottainai.operacional.models.AiChatResponse;
import com.mottainai.operacional.models.ChatMessage;
import com.mottainai.operacional.network.AiApiService;
import com.mottainai.operacional.network.AiRetrofitClient;
import com.mottainai.operacional.network.ApiService;
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
 * O app usa o token Firebase somente para pedir um token curto à API
 * relacional. A chamada à IA recebe exclusivamente esse token de escopo curto.
 */
public class ChatAiFragment extends Fragment {

    private enum RetryAction { NONE, SEND, HISTORY }

    private final ChatMessageAdapter messageAdapter = new ChatMessageAdapter();

    private RecyclerView messagesView;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_ai, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        sessionId = sessionManager.getAiChatSessionId();
        boolean hasSavedSession = sessionId != null && !sessionId.trim().isEmpty();
        if (!hasSavedSession) {
            sessionId = UUID.randomUUID().toString();
        }

        messagesView = view.findViewById(R.id.rv_chat_messages);
        messageInput = view.findViewById(R.id.et_chat_message);
        sendButton = view.findViewById(R.id.btn_chat_send);
        retryButton = view.findViewById(R.id.btn_chat_retry);
        progressBar = view.findViewById(R.id.progress_chat);
        statusView = view.findViewById(R.id.tv_chat_status);
        errorView = view.findViewById(R.id.tv_chat_error);

        messagesView.setLayoutManager(new LinearLayoutManager(requireContext()));
        messagesView.setAdapter(messageAdapter);

        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                Navigation.findNavController(view).popBackStack());
        view.findViewById(R.id.tv_quick_question_1).setOnClickListener(this::sendQuickQuestion);
        view.findViewById(R.id.tv_quick_question_2).setOnClickListener(this::sendQuickQuestion);
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

    private void sendQuickQuestion(View view) {
        if (view instanceof TextView) {
            sendMessage(((TextView) view).getText().toString(), true);
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
        if (requestInProgress) {
            Toast.makeText(requireContext(), "Aguarde a resposta atual.", Toast.LENGTH_SHORT).show();
            return;
        }

        pendingMessage = message;
        if (appendUserMessage) {
            messageAdapter.addMessage(new ChatMessage(message, true));
            messageInput.setText("");
            scrollToLatestMessage();
        }

        showLoading(getString(R.string.chat_sending));
        requestAiAccessToken(RetryAction.SEND, accessToken -> {
            AiApiService aiService = AiRetrofitClient.getClient(accessToken).create(AiApiService.class);
            aiService.sendMessage(new AiChatRequest(pendingMessage, sessionId))
                    .enqueue(new Callback<AiChatResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<AiChatResponse> call,
                                               @NonNull Response<AiChatResponse> response) {
                            if (!isAdded()) return;
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
                            finishRequest();
                            scrollToLatestMessage();
                        }

                        @Override
                        public void onFailure(@NonNull Call<AiChatResponse> call, @NonNull Throwable throwable) {
                            if (!isAdded()) return;
                            showRequestError("Não foi possível conectar à IA. Verifique a conexão e tente novamente.",
                                    RetryAction.SEND);
                        }
                    });
        });
    }

    private void loadHistory() {
        showLoading(getString(R.string.chat_loading_history));
        requestAiAccessToken(RetryAction.HISTORY, accessToken -> {
            AiApiService aiService = AiRetrofitClient.getClient(accessToken).create(AiApiService.class);
            aiService.getHistory(sessionId).enqueue(new Callback<AiChatHistoryResponse>() {
                @Override
                public void onResponse(@NonNull Call<AiChatHistoryResponse> call,
                                       @NonNull Response<AiChatHistoryResponse> response) {
                    if (!isAdded()) return;
                    AiChatHistoryResponse body = response.body();
                    if (!response.isSuccessful() || body == null) {
                        showRequestError(httpMessage("Não foi possível carregar a conversa anterior.", response),
                                RetryAction.HISTORY);
                        return;
                    }
                    messageAdapter.replaceMessages(toChatMessages(body.getMessages()));
                    finishRequest();
                    scrollToLatestMessage();
                }

                @Override
                public void onFailure(@NonNull Call<AiChatHistoryResponse> call, @NonNull Throwable throwable) {
                    if (!isAdded()) return;
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
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            showRequestError("Sua sessão expirou. Entre novamente para conversar com a IA.", errorAction);
            return;
        }

        firebaseUser.getIdToken(false).addOnCompleteListener(task -> {
            if (!isAdded()) return;
            if (!task.isSuccessful() || task.getResult() == null || task.getResult().getToken() == null) {
                showRequestError("Não foi possível validar sua sessão. Entre novamente e tente de novo.",
                        errorAction);
                return;
            }

            GetTokenResult tokenResult = task.getResult();
            sessionManager.saveToken(tokenResult.getToken());
            RetrofitClient.clear();

            ApiService apiService = RetrofitClient.getClient(requireContext()).create(ApiService.class);
            apiService.issueAiAccessToken().enqueue(new Callback<AiAccessTokenResponse>() {
                @Override
                public void onResponse(@NonNull Call<AiAccessTokenResponse> call,
                                       @NonNull Response<AiAccessTokenResponse> response) {
                    if (!isAdded()) return;
                    AiAccessTokenResponse body = response.body();
                    if (!response.isSuccessful() || body == null
                            || body.getAccessToken() == null || body.getAccessToken().trim().isEmpty()) {
                        showRequestError(httpMessage("A API não autorizou o acesso à IA.", response),
                                errorAction);
                        return;
                    }
                    callback.onToken(body.getAccessToken());
                }

                @Override
                public void onFailure(@NonNull Call<AiAccessTokenResponse> call, @NonNull Throwable throwable) {
                    if (!isAdded()) return;
                    showRequestError("Não foi possível conectar à API para autorizar a IA.", errorAction);
                }
            });
        });
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
            messagesView.post(() -> messagesView.smoothScrollToPosition(messageAdapter.getItemCount() - 1));
        }
    }

    private interface AiTokenCallback {
        void onToken(String accessToken);
    }
}

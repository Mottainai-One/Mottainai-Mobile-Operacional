package com.mottainai.operacional.repository;

import android.content.Context;
import com.google.firebase.firestore.ListenerRegistration;
import com.mottainai.operacional.models.ApiSuggestionResponse;
import com.mottainai.operacional.models.Suggestion;
import com.mottainai.operacional.network.ApiService;
import com.mottainai.operacional.network.RetrofitClient;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SuggestionRepository {
    private final ApiService api;
    public interface SuggestionCallback { void onSuccess(List<Suggestion> suggestions); void onError(Exception e); }
    public SuggestionRepository(Context context) { api = RetrofitClient.getClient(context).create(ApiService.class); }

    public ListenerRegistration listenSuggestions(String storeId, SuggestionCallback callback) {
        final int numericStoreId;
        try { numericStoreId = Integer.parseInt(storeId); }
        catch (Exception error) { callback.onError(new IllegalArgumentException("Loja operacional inválida.")); return () -> {}; }
        Call<List<ApiSuggestionResponse>> call = api.getSuggestions(numericStoreId);
        call.enqueue(new Callback<List<ApiSuggestionResponse>>() {
            @Override public void onResponse(Call<List<ApiSuggestionResponse>> call, Response<List<ApiSuggestionResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError(new IllegalStateException("API de sugestões respondeu HTTP " + response.code())); return;
                }
                List<Suggestion> items = new ArrayList<>();
                for (ApiSuggestionResponse dto : response.body()) {
                    if (dto != null && (dto.status == null || "PENDING".equalsIgnoreCase(dto.status))) {
                        items.add(Suggestion.fromApi(dto));
                    }
                }
                callback.onSuccess(items);
            }
            @Override public void onFailure(Call<List<ApiSuggestionResponse>> call, Throwable error) {
                callback.onError(new Exception("Falha ao consultar sugestões da API operacional.", error));
            }
        });
        return call::cancel;
    }
}

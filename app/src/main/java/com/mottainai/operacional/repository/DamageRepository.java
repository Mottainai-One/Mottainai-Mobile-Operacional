package com.mottainai.operacional.repository;

import android.app.Application;

import com.mottainai.operacional.models.Damage;
import com.mottainai.operacional.models.DamageRequest;
import com.mottainai.operacional.network.ApiService;
import com.mottainai.operacional.network.RetrofitClient;
import com.mottainai.operacional.utils.SessionManager;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Persists damage reports through the authenticated operational API.
 */
public class DamageRepository {
    private final ApiService apiService;
    private final SessionManager sessionManager;

    public interface DamageCallback {
        void onSuccess(Damage damage);
        void onError(String message);
    }

    public DamageRepository(Application application) {
        this.apiService = RetrofitClient.getClient(application).create(ApiService.class);
        this.sessionManager = new SessionManager(application);
    }

    public void createDamage(DamageRequest request, DamageCallback callback) {
        if (sessionManager.getApiAccessToken() == null) {
            callback.onError("Entre novamente para enviar a avaria à API.");
            return;
        }
        apiService.createDamage(request).enqueue(new Callback<Damage>() {
            @Override public void onResponse(Call<Damage> call, Response<Damage> response) {
                if (response.isSuccessful() && response.body() != null) callback.onSuccess(response.body());
                else callback.onError(mapError(response.code()));
            }
            @Override public void onFailure(Call<Damage> call, Throwable t) { callback.onError("Erro de rede: " + t.getMessage()); }
        });
    }

    private String mapError(int code) {
        if (code==401) return "Sessão expirada. Faça login novamente.";
        if (code==403) return "Sem permissão para registrar avaria.";
        if (code==404) return "Produto não encontrado.";
        if (code==409) return "Conflito de estoque.";
        if (code==400) return "Dados inválidos.";
        if (code>=500) return "Erro no servidor. Tente novamente.";
        return "Erro: " + code;
    }

}

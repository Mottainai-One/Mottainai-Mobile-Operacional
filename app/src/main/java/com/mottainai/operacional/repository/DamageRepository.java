package com.mottainai.operacional.repository;

import android.app.Application;

import com.mottainai.operacional.models.Damage;
import com.mottainai.operacional.models.DamageRequest;
import com.mottainai.operacional.network.ApiService;
import com.mottainai.operacional.network.RetrofitClient;
import com.mottainai.operacional.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository mockável. Se USE_MOCK_DAMAGE=true, simula POST /api/v1/damages localmente.
 * Quando backend existir, usar ApiService.postDamage.
 */
public class DamageRepository {
    private final ApiService apiService;
    private static final List<Damage> mockDamages = new ArrayList<>();

    public interface DamageCallback {
        void onSuccess(Damage damage);
        void onError(String message);
    }

    public DamageRepository(Application application) {
        this.apiService = RetrofitClient.getClient(application).create(ApiService.class);
    }

    public void createDamage(DamageRequest request, DamageCallback callback) {
        if (Constants.USE_MOCK_DAMAGE) {
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                // Validação servidor simulada
                if (request.getProductId() == null) { callback.onError("Produto inválido"); return; }
                Damage d = new Damage();
                d.setId(UUID.randomUUID().toString());
                d.setProductId(request.getProductId());
                d.setStoreId("loja02");
                d.setUserId("mock-user");
                d.setReason(request.getReason());
                d.setQuantity(request.getQuantity());
                d.setNote(request.getNote());
                d.setPhotoUrl(request.getPhotoUrl());
                d.setStatus("registered");
                d.setCreatedAt(java.time.Instant.now().toString());
                mockDamages.add(d);
                callback.onSuccess(d);
            }, 900);
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
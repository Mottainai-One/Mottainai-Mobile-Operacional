package com.mottainai.operacional.repository;

import android.content.Context;
import com.google.firebase.firestore.ListenerRegistration;
import com.mottainai.operacional.models.Alert;
import com.mottainai.operacional.models.ApiAlertResponse;
import com.mottainai.operacional.network.ApiService;
import com.mottainai.operacional.network.RetrofitClient;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlertRepository {
    private final ApiService api;
    public interface AlertCallback { void onSuccess(List<Alert> alerts); void onError(Exception e); }
    public AlertRepository(Context context) { api = RetrofitClient.getClient(context).create(ApiService.class); }

    public ListenerRegistration listenAlerts(String storeId, int days, AlertCallback callback) {
        final int numericStoreId;
        try { numericStoreId = Integer.parseInt(storeId); }
        catch (Exception error) { callback.onError(new IllegalArgumentException("Loja operacional inválida.")); return () -> {}; }
        Call<List<ApiAlertResponse>> call = api.getAlerts(numericStoreId);
        call.enqueue(new Callback<List<ApiAlertResponse>>() {
            @Override public void onResponse(Call<List<ApiAlertResponse>> call, Response<List<ApiAlertResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError(new IllegalStateException("API de alertas respondeu HTTP " + response.code())); return;
                }
                long cutoff = System.currentTimeMillis() - days * 86_400_000L;
                List<Alert> alerts = new ArrayList<>();
                for (ApiAlertResponse dto : response.body()) {
                    Alert alert = Alert.fromApi(dto);
                    if (alert != null && alert.getCreatedAt() >= cutoff) alerts.add(alert);
                }
                callback.onSuccess(alerts);
            }
            @Override public void onFailure(Call<List<ApiAlertResponse>> call, Throwable error) {
                callback.onError(new Exception("Falha ao consultar alertas da API operacional.", error));
            }
        });
        return call::cancel;
    }
}

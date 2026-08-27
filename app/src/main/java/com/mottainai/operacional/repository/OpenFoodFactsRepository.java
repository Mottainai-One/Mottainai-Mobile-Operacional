package com.mottainai.operacional.repository;

import com.mottainai.operacional.models.OpenFoodFactsResponse;
import com.mottainai.operacional.network.OpenFoodFactsApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Integração isolada com Open Food Facts (fonte pública, sem chave).
 * Não envia dados pessoais ou storeId. User-Agent conforme política da API.
 */
public class OpenFoodFactsRepository {

    private static final String BASE_URL = "https://world.openfoodfacts.org/";
    private static final String USER_AGENT = "MottainaiOperacional/1.0 (Android; contact@mottainai.com.br)";

    private final OpenFoodFactsApiService api;

    public OpenFoodFactsRepository() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(OpenFoodFactsApiService.class);
    }

    public interface CallbackResult {
        void onSuccess(OpenFoodFactsResponse.ProductData product);
        void onNotFound();
        void onError(String msg);
    }

    public void fetchByBarcode(String barcode, CallbackResult cb) {
        if (barcode == null || barcode.trim().isEmpty()) {
            cb.onError("Código vazio");
            return;
        }
        api.getProductByBarcode(barcode.trim(), USER_AGENT).enqueue(new Callback<OpenFoodFactsResponse>() {
            @Override public void onResponse(Call<OpenFoodFactsResponse> call, Response<OpenFoodFactsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isFound()) {
                    cb.onSuccess(response.body().getProduct());
                } else if (response.isSuccessful() && response.body() != null && !response.body().isFound()) {
                    cb.onNotFound();
                } else {
                    cb.onError("Produto não encontrado na base pública");
                }
            }
            @Override public void onFailure(Call<OpenFoodFactsResponse> call, Throwable t) {
                cb.onError("Sem conexão com fonte externa: " + t.getMessage());
            }
        });
    }
}
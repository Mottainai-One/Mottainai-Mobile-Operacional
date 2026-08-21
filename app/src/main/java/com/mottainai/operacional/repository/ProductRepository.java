package com.mottainai.operacional.repository;

import android.app.Application;

import com.mottainai.operacional.models.Product;
import com.mottainai.operacional.network.ApiService;
import com.mottainai.operacional.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductRepository {

    private final ApiService apiService;

    public ProductRepository(Application application) {
        this.apiService = RetrofitClient.getClient(application).create(ApiService.class);
    }

    public interface ProductListCallback {
        void onSuccess(List<Product> products);
        void onError(String message);
    }

    public interface ProductCallback {
        void onSuccess(Product product);
        void onError(String message);
    }

    public void fetchProducts(String storeId, ProductListCallback callback) {
        apiService.getProducts(storeId).enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Erro ao carregar produtos: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                callback.onError("Erro de rede: " + t.getMessage());
            }
        });
    }

    public void fetchProductById(String productId, ProductCallback callback) {
        apiService.getProduct(productId).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Produto não encontrado: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                callback.onError("Erro de rede: " + t.getMessage());
            }
        });
    }

    public void createProduct(Product product, ProductCallback callback) {
        apiService.createProduct(product).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Erro ao criar produto: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                callback.onError("Erro de rede: " + t.getMessage());
            }
        });
    }

    public void updateProduct(String productId, Product product, ProductCallback callback) {
        apiService.updateProduct(productId, product).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Erro ao atualizar produto: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                callback.onError("Erro de rede: " + t.getMessage());
            }
        });
    }
}
package com.mottainai.operacional.repository;

import android.app.Application;

import com.mottainai.operacional.models.PageResponse;
import com.mottainai.operacional.models.Product;
import com.mottainai.operacional.models.ProductResponse;
import com.mottainai.operacional.network.ApiService;
import com.mottainai.operacional.network.RetrofitClient;

import java.util.ArrayList;
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

    /** Converte DTO da API para modelo de domínio usado pela UI. */
    private Product map(ProductResponse dto) {
        Product p = new Product();
        if (dto.getId() != null) p.setId(String.valueOf(dto.getId()));
        p.setName(dto.getName());
        // SKU na UI = barcode da API (código escaneável). Campo barcode é a fonte pública.
        p.setSku(dto.getBarcode());
        // Campos de inventário não retornados por /api/v1/products.
        // TODO contrato pendente: quantity, minQuantity, batch, expiryDate, storeId
        // virão de /api/v1/inventory ou endpoint dedicado. Mantidos como 0/null até existir.
        // supplier: brand da API pode ser exibido como fornecedor temporariamente
        p.setSupplier(dto.getBrand());
        // storeId: deve ser derivado do Firebase ID token no backend, não enviado pelo cliente.
        // Não confiar em storeId livre; manter null até contrato confirmar filtro por loja.
        // imageUrl, batch, expiryDate: sem contrato, permanecem null/vazio.
        p.setImageUrl(null);
        p.setBatch(null);
        p.setExpiryDate(null);
        p.setStoreId(null);
        p.setQuantity(0);
        p.setMinQuantity(0);
        return p;
    }

    private List<Product> mapList(List<ProductResponse> dtos) {
        List<Product> out = new ArrayList<>();
        if (dtos != null) {
            for (ProductResponse dto : dtos) out.add(map(dto));
        }
        return out;
    }

    /** Lista produtos. Backend pode retornar Page; convertemos para List para UI. */
    public void fetchProducts(String storeId, ProductListCallback callback) {
        // storeId ignorado aqui porque API Spring deriva loja do token; ver contrato pendente.
        apiService.getProductsPaged(0, 50).enqueue(new Callback<PageResponse<ProductResponse>>() {
            @Override
            public void onResponse(Call<PageResponse<ProductResponse>> call, Response<PageResponse<ProductResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getContent() != null) {
                    callback.onSuccess(mapList(response.body().getContent()));
                } else {
                    callback.onError(mapHttpError(response.code()));
                }
            }

            @Override
            public void onFailure(Call<PageResponse<ProductResponse>> call, Throwable t) {
                callback.onError("Erro de rede: " + t.getMessage());
            }
        });
    }

    /** Busca com paginação e termo de busca (nome/barcode). Se backend não suportar search, faz filtro local na página carregada. */
    public void fetchProducts(int page, int size, String search, ProductListCallback callback) {
        apiService.getProducts(page, size, null, search).enqueue(new Callback<PageResponse<ProductResponse>>() {
            @Override
            public void onResponse(Call<PageResponse<ProductResponse>> call, Response<PageResponse<ProductResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getContent() != null) {
                    callback.onSuccess(mapList(response.body().getContent()));
                } else {
                    callback.onError(mapHttpError(response.code()));
                }
            }

            @Override
            public void onFailure(Call<PageResponse<ProductResponse>> call, Throwable t) {
                callback.onError("Erro de rede: " + t.getMessage());
            }
        });
    }

    public void fetchProductById(String productId, ProductCallback callback) {
        apiService.getProduct(productId).enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Verificação de escopo por loja deve ser feita no backend.
                    callback.onSuccess(map(response.body()));
                } else {
                    callback.onError(mapHttpError(response.code()));
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                callback.onError("Erro de rede: " + t.getMessage());
            }
        });
    }

    public void fetchProductByBarcode(String barcode, ProductCallback callback) {
        apiService.getProductByBarcode(barcode).enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(map(response.body()));
                } else {
                    callback.onError(mapHttpError(response.code()));
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                callback.onError("Erro de rede: " + t.getMessage());
            }
        });
    }

    // Compat para Mock: Product -> UpsertRequest
    public void createProduct(Product product, ProductCallback callback) {
        com.mottainai.operacional.models.ProductUpsertRequest r = new com.mottainai.operacional.models.ProductUpsertRequest();
        r.setName(product.getName()); r.setBarcode(product.getSku()); r.setBrand(product.getSupplier());
        r.setDescription(null); r.setCategoryId(1); r.setUnitMeasure("UN"); r.setActive(true);
        createProduct(r, callback);
    }

    public void createProduct(com.mottainai.operacional.models.ProductUpsertRequest request, ProductCallback callback) {
        apiService.createProduct(request).enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(map(response.body()));
                } else {
                    callback.onError(mapHttpError(response.code()));
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                callback.onError("Erro de rede: " + t.getMessage());
            }
        });
    }

    public void updateProduct(String productId, Product product, ProductCallback callback) {
        com.mottainai.operacional.models.ProductUpsertRequest r = new com.mottainai.operacional.models.ProductUpsertRequest();
        r.setName(product.getName()); r.setBarcode(product.getSku()); r.setBrand(product.getSupplier());
        r.setDescription(null); r.setCategoryId(1); r.setUnitMeasure("UN"); r.setActive(true);
        updateProduct(productId, r, callback);
    }

    public void updateProduct(String productId, com.mottainai.operacional.models.ProductUpsertRequest request, ProductCallback callback) {
        apiService.updateProduct(productId, request).enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(map(response.body()));
                } else {
                    callback.onError(mapHttpError(response.code()));
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                callback.onError("Erro de rede: " + t.getMessage());
            }
        });
    }

    private String mapHttpError(int code) {
        if (code == 401) return "Sessão expirada. Faça login novamente.";
        if (code == 403) return "Sem permissão para esta operação.";
        if (code == 404) return "Produto não encontrado.";
        if (code >= 500) return "Erro no servidor. Tente novamente.";
        return "Erro ao carregar produtos: " + code;
    }
}
package com.mottainai.operacional.repository;

import android.app.Application;

import com.mottainai.operacional.models.PageResponse;
import com.mottainai.operacional.models.Product;
import com.mottainai.operacional.models.ProductResponse;
import com.mottainai.operacional.models.ProductUpsertRequest;
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
        p.setUnitMeasure(dto.getUnitMeasure());
        p.setWeight(dto.getWeight());
        p.setCategoryId(dto.getCategoryId());
        p.setTaxProfileId(dto.getTaxProfileId());
        p.setNcm(dto.getNcm());
        p.setCest(dto.getCest());
        p.setVersion(dto.getVersion());
        p.setActive(Boolean.TRUE.equals(dto.getActive()));
        // SKU na UI = barcode da API (código escaneável). Campo barcode é a fonte pública.
        p.setSku(dto.getBarcode());
        // Campos de inventário não retornados por /api/v1/products.
        // TODO contrato pendente: quantity, minQuantity, batch, expiryDate, storeId
        // virão de /api/v1/inventory ou endpoint dedicado. Mantidos como 0/null até existir.
        // supplier: brand da API pode ser exibido como fornecedor temporariamente
        p.setSupplier(dto.getBrand());
        // storeId: deve ser derivado do Firebase ID token no backend, não enviado pelo cliente.
        // Não confiar em storeId livre; manter null até contrato confirmar filtro por loja.
        // Imagens vêm da imageUrl persistida pela API; lote e validade ainda não fazem parte deste contrato.
        p.setImageUrl(dto.getImageUrl());
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

    /** Carrega o catálogo completo para a lista e sua busca local. */
    public void fetchProducts(String storeId, ProductListCallback callback) {
        fetchCatalogPage(0, new ArrayList<>(), callback);
    }

    private void fetchCatalogPage(int page, List<Product> products, ProductListCallback callback) {
        apiService.getProducts(page, 50, "id,asc", null).enqueue(new Callback<PageResponse<ProductResponse>>() {
            @Override
            public void onResponse(Call<PageResponse<ProductResponse>> call, Response<PageResponse<ProductResponse>> response) {
                PageResponse<ProductResponse> body = response.body();
                if (!response.isSuccessful() || body == null || body.getContent() == null) {
                    callback.onError(mapHttpError(response.code()));
                    return;
                }
                if (body.getNumber() != page) {
                    callback.onError("A API retornou uma página de produtos inesperada.");
                    return;
                }
                products.addAll(mapList(body.getContent()));
                if (body.isLast() || page + 1 >= body.getTotalPages()) {
                    callback.onSuccess(products);
                } else if (body.getContent().isEmpty()) {
                    callback.onError("A API retornou uma página vazia antes do fim do catálogo.");
                } else {
                    fetchCatalogPage(page + 1, products, callback);
                }
            }

            @Override
            public void onFailure(Call<PageResponse<ProductResponse>> call, Throwable t) {
                callback.onError("Não foi possível carregar o catálogo completo. Tente novamente.");
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

    /**
     * O modelo legado {@link Product} não representa todos os campos do contrato
     * de produto. Em particular, SKU e fornecedor não podem ser enviados como
     * barcode e marca. As gravações usam ProductCreateRequest/ProductUpdateRequest.
     */
    @Deprecated
    public void createProduct(Product product, ProductCallback callback) {
        callback.onError("Use ProductCreateRequest para cadastrar produtos na API.");
    }

    public void createProduct(com.mottainai.operacional.models.ProductCreateRequest request, ProductCallback callback) {
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

    /** Legacy mock-fixture contract. Real form code uses ProductCreateRequest. */
    @Deprecated
    public void createProduct(com.mottainai.operacional.models.ProductUpsertRequest request, ProductCallback callback) {
        callback.onError("Use ProductCreateRequest para cadastrar produtos na API.");
    }

    /** @see #createProduct(Product, ProductCallback) */
    @Deprecated
    public void updateProduct(String productId, Product product, ProductCallback callback) {
        callback.onError("Use ProductUpdateRequest para editar produtos na API.");
    }

    public void updateProduct(String productId, com.mottainai.operacional.models.ProductUpdateRequest request, ProductCallback callback) {
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

    /** Legacy mock-fixture contract. Real form code uses ProductUpdateRequest. */
    @Deprecated
    public void updateProduct(String productId, com.mottainai.operacional.models.ProductUpsertRequest request, ProductCallback callback) {
        callback.onError("Use ProductUpdateRequest para editar produtos na API.");
    }

    private String mapHttpError(int code) {
        if (code == 401) return "Sessão expirada. Faça login novamente.";
        if (code == 403) return "Sem permissão para esta operação.";
        if (code == 404) return "Produto não encontrado.";
        if (code >= 500) return "Erro no servidor. Tente novamente.";
        return "Erro ao carregar produtos: " + code;
    }
}

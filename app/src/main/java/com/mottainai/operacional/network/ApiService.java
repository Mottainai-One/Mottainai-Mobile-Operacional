package com.mottainai.operacional.network;

import com.mottainai.operacional.models.PageResponse;
import com.mottainai.operacional.models.Product;
import com.mottainai.operacional.models.ProductResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // Produtos - alinhado com API Spring (/api/v1)
    @GET("api/v1/products")
    Call<PageResponse<ProductResponse>> getProducts(
            @Query("page") int page,
            @Query("size") int size,
            @Query("sort") String sort,
            @Query("search") String search
    );

    // Compat: sem paginação explícita (page 0, size 20). Mantém storeId opcional se backend exigir.
    @GET("api/v1/products")
    Call<PageResponse<ProductResponse>> getProductsPaged(
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/v1/products/{id}")
    Call<ProductResponse> getProduct(@Path("id") String id);

    @GET("api/v1/products/barcode/{barcode}")
    Call<ProductResponse> getProductByBarcode(@Path("barcode") String barcode);

    @POST("api/v1/products")
    Call<ProductResponse> createProduct(@Body com.mottainai.operacional.models.ProductUpsertRequest request);

    @PUT("api/v1/products/{id}")
    Call<ProductResponse> updateProduct(@Path("id") String id, @Body com.mottainai.operacional.models.ProductUpsertRequest request);

    // Alertas e sugestões - mantido para compat, mas contrato pendente (devem vir do backend)
    // Se backend não tiver, Repository deve ficar mockável e não simular sucesso.
    @GET("api/v1/alerts")
    Call<java.util.List<com.mottainai.operacional.models.Alert>> getAlerts(@Query("store_id") String storeId);

    @GET("api/v1/suggestions")
    Call<java.util.List<com.mottainai.operacional.models.Suggestion>> getSuggestions(@Query("store_id") String storeId);

    // Avarias - contrato pendente confirmar com backend
    @POST("api/v1/damages")
    Call<com.mottainai.operacional.models.Damage> createDamage(@Body com.mottainai.operacional.models.DamageRequest request);
}
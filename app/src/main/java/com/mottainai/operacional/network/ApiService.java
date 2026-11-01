package com.mottainai.operacional.network;

import com.mottainai.operacional.models.PageResponse;
import com.mottainai.operacional.models.Product;
import com.mottainai.operacional.models.ProductResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("api/v1/auth/login")
    Call<com.mottainai.operacional.models.ApiTokenResponse> exchangeFirebaseToken(
            @Body com.mottainai.operacional.models.ApiLoginRequest request);

    @GET("api/v1/auth/profile")
    Call<com.mottainai.operacional.models.ApiUserProfile> getOperationalProfile();

    @POST("api/v1/auth/refresh")
    Call<com.mottainai.operacional.models.ApiTokenResponse> refreshOperationalToken(
            @Body com.mottainai.operacional.models.ApiRefreshRequest request);

    @POST("api/v1/suggested-actions/{id}/decision")
    Call<com.mottainai.operacional.models.ApiSuggestionResponse> decideSuggestion(
            @Path("id") String id, @Body com.mottainai.operacional.models.ApiSuggestionDecisionRequest request);

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
    Call<ProductResponse> createProduct(@Body com.mottainai.operacional.models.ProductCreateRequest request);

    @PUT("api/v1/products/{id}")
    Call<ProductResponse> updateProduct(@Path("id") String id, @Body com.mottainai.operacional.models.ProductUpdateRequest request);

    // Alertas e sugestões - mantido para compat, mas contrato pendente (devem vir do backend)
    // Se backend não tiver, Repository deve ficar mockável e não simular sucesso.
    @GET("api/v1/alerts")
    Call<java.util.List<com.mottainai.operacional.models.ApiAlertResponse>> getAlerts(@Query("storeId") Integer storeId);

    @GET("api/v1/suggested-actions")
    Call<java.util.List<com.mottainai.operacional.models.ApiSuggestionResponse>> getSuggestions(@Query("storeId") Integer storeId);

    // Avarias - contrato pendente confirmar com backend
    @POST("api/v1/damages")
    Call<com.mottainai.operacional.models.Damage> createDamage(@Body com.mottainai.operacional.models.DamageRequest request);

    @GET("api/v1/damages")
    Call<java.util.List<com.mottainai.operacional.models.Damage>> getDamages();

    @GET("api/v1/inventory")
    Call<java.util.List<com.mottainai.operacional.models.ApiInventoryResponse>> getInventory(@Query("storeId") Integer storeId);

    @GET("api/v1/batches")
    Call<java.util.List<com.mottainai.operacional.models.ApiBatchResponse>> getBatches(@Query("storeId") Integer storeId);

    @GET("api/v1/inventory-counts")
    Call<java.util.List<com.mottainai.operacional.models.ApiInventoryCountResponse>> getInventoryCounts();

    @GET("api/v1/inventory-counts/{id}")
    Call<com.mottainai.operacional.models.ApiInventoryCountResponse> getInventoryCount(@Path("id") String id);

    @POST("api/v1/inventory-counts")
    Call<com.mottainai.operacional.models.ApiInventoryCountResponse> createInventoryCount(@Body com.mottainai.operacional.models.ApiInventoryCountCreateRequest request);

    @POST("api/v1/inventory-counts/{id}/items")
    Call<com.mottainai.operacional.models.ApiInventoryCountResponse.Item> addInventoryCountItem(
            @Path("id") String id, @Body com.mottainai.operacional.models.ApiInventoryCountItemRequest request);

    @PUT("api/v1/inventory-counts/{id}/items/{itemId}")
    Call<com.mottainai.operacional.models.ApiInventoryCountResponse.Item> updateInventoryCountItem(
            @Path("id") String id, @Path("itemId") String itemId,
            @Body com.mottainai.operacional.models.ApiInventoryCountItemUpdateRequest request);

    @DELETE("api/v1/inventory-counts/{id}/items/{itemId}")
    Call<Void> deleteInventoryCountItem(@Path("id") String id, @Path("itemId") String itemId);

    @POST("api/v1/inventory-counts/{id}/finish")
    Call<com.mottainai.operacional.models.ApiInventoryCountResponse> finishInventoryCount(
            @Path("id") String id, @Header("Idempotency-Key") String idempotencyKey);
}

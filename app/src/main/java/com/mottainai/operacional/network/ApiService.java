package com.mottainai.operacional.network;

import com.mottainai.operacional.models.Product;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @GET("products")
    Call<List<Product>> getProducts(@Query("store_id") String storeId);

    @GET("products/{id}")
    Call<Product> getProduct(@Path("id") String id);

    @POST("products")
    Call<Product> createProduct(@Body Product product);

    @PUT("products/{id}")
    Call<Product> updateProduct(@Path("id") String id, @Body Product product);

    @GET("alerts")
    Call<List<com.mottainai.operacional.models.Alert>> getAlerts(@Query("store_id") String storeId);

    @GET("suggestions")
    Call<List<com.mottainai.operacional.models.Suggestion>> getSuggestions(@Query("store_id") String storeId);
}
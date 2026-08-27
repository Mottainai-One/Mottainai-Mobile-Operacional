package com.mottainai.operacional.network;

import com.mottainai.operacional.models.OpenFoodFactsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Header;

public interface OpenFoodFactsApiService {

    @GET("api/v0/product/{barcode}.json")
    Call<OpenFoodFactsResponse> getProductByBarcode(
            @Path("barcode") String barcode,
            @Header("User-Agent") String userAgent
    );
}
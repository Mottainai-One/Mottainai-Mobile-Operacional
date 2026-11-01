package com.mottainai.operacional.repository;

import android.content.Context;

import com.mottainai.operacional.models.SalesSummaryResponse;
import com.mottainai.operacional.network.ApiService;
import com.mottainai.operacional.network.RetrofitClient;

import retrofit2.Call;

public class SalesSummaryRepository {
    private final ApiService api;

    public SalesSummaryRepository(Context context) {
        api = RetrofitClient.getClient(context).create(ApiService.class);
    }

    public Call<SalesSummaryResponse> getCurrentMonth(Integer storeId) {
        return api.getCurrentMonthSalesSummary(storeId);
    }
}

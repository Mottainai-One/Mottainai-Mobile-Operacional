package com.mottainai.operacional.models;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class SalesSummaryResponse {
    @SerializedName("storeId") private Integer storeId;
    @SerializedName("month") private String month;
    @SerializedName("transactionCount") private long transactionCount;
    @SerializedName("totalAmount") private BigDecimal totalAmount;

    public Integer getStoreId() { return storeId; }
    public String getMonth() { return month; }
    public long getTransactionCount() { return transactionCount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
}

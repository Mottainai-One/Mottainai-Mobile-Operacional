package com.mottainai.operacional.models;
import com.google.gson.annotations.SerializedName;
public class ApiBatchResponse {
 @SerializedName("id") public Integer id; @SerializedName("productId") public Integer productId;
 @SerializedName("batchCode") public String batchCode; @SerializedName("expirationDate") public String expirationDate;
}

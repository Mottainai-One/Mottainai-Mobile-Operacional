package com.mottainai.operacional.models;
import com.google.gson.annotations.SerializedName;
public class ApiInventoryResponse {
 @SerializedName("id") public Integer id; @SerializedName("storeId") public Integer storeId;
 @SerializedName("batchId") public Integer batchId; @SerializedName("currentQuantity") public Double currentQuantity;
 @SerializedName("minimumQuantity") public Double minimumQuantity; @SerializedName("version") public Integer version;
}

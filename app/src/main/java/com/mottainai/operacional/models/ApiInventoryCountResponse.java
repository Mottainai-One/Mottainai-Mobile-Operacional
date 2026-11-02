package com.mottainai.operacional.models;
import com.google.gson.annotations.SerializedName;
import java.util.List;
public class ApiInventoryCountResponse {
 @SerializedName("id") public Long id; @SerializedName("storeId") public Integer storeId;
 @SerializedName("employeeId") public Integer employeeId; @SerializedName("status") public String status;
 @SerializedName("startedAt") public String startedAt; @SerializedName("finishedAt") public String finishedAt;
 @SerializedName("observation") public String observation; @SerializedName("items") public List<Item> items;
 public static class Item {
  @SerializedName("id") public Long id; @SerializedName("inventoryId") public Integer inventoryId;
  @SerializedName("systemQuantity") public Double systemQuantity; @SerializedName("countedQuantity") public Double countedQuantity;
  @SerializedName("observation") public String observation;
 }
}

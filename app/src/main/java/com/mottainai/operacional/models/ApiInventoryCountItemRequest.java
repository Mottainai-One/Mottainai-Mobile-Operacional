package com.mottainai.operacional.models;
import com.google.gson.annotations.SerializedName;
public class ApiInventoryCountItemRequest {
 @SerializedName("inventoryId") private final Integer inventoryId;
 @SerializedName("countedQuantity") private final Integer countedQuantity;
 @SerializedName("observation") private final String observation;
 public ApiInventoryCountItemRequest(Integer inventoryId, Integer countedQuantity, String observation) {
  this.inventoryId = inventoryId; this.countedQuantity = countedQuantity; this.observation = observation;
 }
}

package com.mottainai.operacional.models;
import com.google.gson.annotations.SerializedName;
public class ApiInventoryCountItemUpdateRequest {
 @SerializedName("countedQuantity") private final Integer countedQuantity;
 @SerializedName("observation") private final String observation;
 public ApiInventoryCountItemUpdateRequest(Integer countedQuantity, String observation) {
  this.countedQuantity = countedQuantity; this.observation = observation;
 }
}

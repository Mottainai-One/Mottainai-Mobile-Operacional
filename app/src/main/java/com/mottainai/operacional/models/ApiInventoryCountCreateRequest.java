package com.mottainai.operacional.models;
import com.google.gson.annotations.SerializedName;
public class ApiInventoryCountCreateRequest {
 @SerializedName("observation") private final String observation;
 public ApiInventoryCountCreateRequest(String observation) { this.observation = observation; }
}

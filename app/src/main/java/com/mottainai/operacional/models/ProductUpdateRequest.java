package com.mottainai.operacional.models;
import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
public class ProductUpdateRequest {
 @SerializedName("categoryId") private final Integer categoryId;
 @SerializedName("taxProfileId") private final Integer taxProfileId;
 @SerializedName("ncm") private final String ncm;
 @SerializedName("cest") private final String cest;
 @SerializedName("name") private final String name;
 @SerializedName("description") private final String description;
 @SerializedName("brand") private final String brand;
 @SerializedName("unitMeasure") private final String unitMeasure;
 @SerializedName("weight") private final BigDecimal weight;
 @SerializedName("active") private final Boolean active;
 @SerializedName("version") private final Integer version;
 @SerializedName("imageUrl") private final String imageUrl;
 public ProductUpdateRequest(Integer categoryId, Integer taxProfileId, String ncm, String cest, String name,
     String description, String brand, String unitMeasure, BigDecimal weight, Boolean active, Integer version, String imageUrl) {
  this.categoryId=categoryId; this.taxProfileId=taxProfileId; this.ncm=ncm; this.cest=cest; this.name=name;
  this.description=description; this.brand=brand; this.unitMeasure=unitMeasure; this.weight=weight;
  this.active=active; this.version=version; this.imageUrl=imageUrl;
 }
}

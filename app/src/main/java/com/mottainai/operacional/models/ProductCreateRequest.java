package com.mottainai.operacional.models;
import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
public class ProductCreateRequest {
 @SerializedName("categoryId") private final Integer categoryId;
 @SerializedName("taxProfileId") private final Integer taxProfileId;
 @SerializedName("barcode") private final String barcode;
 @SerializedName("ncm") private final String ncm;
 @SerializedName("cest") private final String cest;
 @SerializedName("name") private final String name;
 @SerializedName("description") private final String description;
 @SerializedName("brand") private final String brand;
 @SerializedName("unitMeasure") private final String unitMeasure;
 @SerializedName("weight") private final BigDecimal weight;
 @SerializedName("imageUrl") private final String imageUrl;
 public ProductCreateRequest(Integer categoryId, Integer taxProfileId, String barcode, String ncm, String cest,
     String name, String description, String brand, String unitMeasure, BigDecimal weight, String imageUrl) {
  this.categoryId=categoryId; this.taxProfileId=taxProfileId; this.barcode=barcode; this.ncm=ncm; this.cest=cest;
  this.name=name; this.description=description; this.brand=brand; this.unitMeasure=unitMeasure; this.weight=weight; this.imageUrl=imageUrl;
 }
}

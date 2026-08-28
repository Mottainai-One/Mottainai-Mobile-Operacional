package com.mottainai.operacional.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

/**
 * DTO de resposta da API Spring (/api/v1/products).
 * Campos observados no backend: id, categoryId, categoryName, barcode, name,
 * description, brand, unitMeasure, weight, active, version.
 * Não contém campos de inventário (quantity, minQuantity, batch, expiry_date, storeId)
 * — ver Product.java e contrato pendente.
 */
public class ProductResponse {

    @SerializedName("id")
    private Integer id;

    @SerializedName("categoryId")
    private Integer categoryId;

    @SerializedName("categoryName")
    private String categoryName;

    @SerializedName("barcode")
    private String barcode;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("brand")
    private String brand;

    @SerializedName("unitMeasure")
    private String unitMeasure;

    @SerializedName("weight")
    private BigDecimal weight;

    @SerializedName("active")
    private Boolean active;

    @SerializedName("version")
    private Integer version;

    public Integer getId() { return id; }
    public Integer getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public String getBarcode() { return barcode; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getBrand() { return brand; }
    public String getUnitMeasure() { return unitMeasure; }
    public BigDecimal getWeight() { return weight; }
    public Boolean getActive() { return active; }
    public Integer getVersion() { return version; }
}
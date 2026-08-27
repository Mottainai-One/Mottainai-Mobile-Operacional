package com.mottainai.operacional.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

/**
 * Payload enviado para POST /api/v1/products e PUT /api/v1/products/{id}.
 * Mapeia exatamente CreateProductRequest / UpdateProductRequest do backend.
 * Campos de inventário (quantity, minQuantity, batch, expiryDate, supplier) NÃO são enviados
 * aqui — pendentes em /api/v1/inventory (documentado em Product.java).
 */
public class ProductUpsertRequest {

    @SerializedName("categoryId")
    private Integer categoryId;

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

    // Para PUT: version para controle otimista (409 se desatualizado)
    @SerializedName("version")
    private Integer version;

    public ProductUpsertRequest() {}

    // Getters/Setters
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getUnitMeasure() { return unitMeasure; }
    public void setUnitMeasure(String unitMeasure) { this.unitMeasure = unitMeasure; }
    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}
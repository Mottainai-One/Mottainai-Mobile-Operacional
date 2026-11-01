package com.mottainai.operacional.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

/**
 * Product catalog fields plus stock data populated from the operational API.
 */
public class Product {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("sku")
    private String sku;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("min_quantity")
    private int minQuantity;

    @SerializedName("batch")
    private String batch;

    @SerializedName("expiry_date")
    private String expiryDate;

    @SerializedName("supplier")
    private String supplier;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("store_id")
    private String storeId;

    private Integer categoryId;
    private Integer taxProfileId;
    private String ncm;
    private String cest;
    private Integer version;
    private boolean active = true;
    private String unitMeasure;
    private BigDecimal weight;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    public Product() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getMinQuantity() { return minQuantity; }
    public void setMinQuantity(int minQuantity) { this.minQuantity = minQuantity; }

    public String getBatch() { return batch; }
    public void setBatch(String batch) { this.batch = batch; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public Integer getTaxProfileId() { return taxProfileId; }
    public void setTaxProfileId(Integer taxProfileId) { this.taxProfileId = taxProfileId; }
    public String getNcm() { return ncm; }
    public void setNcm(String ncm) { this.ncm = ncm; }
    public String getCest() { return cest; }
    public void setCest(String cest) { this.cest = cest; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getUnitMeasure() { return unitMeasure; }
    public void setUnitMeasure(String unitMeasure) { this.unitMeasure = unitMeasure; }
    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public boolean isLowStock() {
        return quantity <= minQuantity && minQuantity > 0;
    }

    public boolean isAttentionStock() {
        return quantity > minQuantity && quantity <= minQuantity * 1.5 && minQuantity > 0;
    }
}

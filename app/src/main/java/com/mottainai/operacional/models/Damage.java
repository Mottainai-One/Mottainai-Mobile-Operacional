package com.mottainai.operacional.models;

import com.google.gson.annotations.SerializedName;

public class Damage {
    @SerializedName("id") private String id;
    @SerializedName("productId") private String productId;
    @SerializedName("storeId") private String storeId;
    @SerializedName("userId") private String userId;
    @SerializedName("reason") private String reason;
    @SerializedName("quantity") private int quantity;
    @SerializedName("note") private String note;
    @SerializedName("photoUrl") private String photoUrl;
    @SerializedName("createdAt") private String createdAt;
    @SerializedName("status") private String status;

    public Damage() {}
    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getProductId() { return productId; } public void setProductId(String v) { productId = v; }
    public String getStoreId() { return storeId; } public void setStoreId(String v) { storeId = v; }
    public String getUserId() { return userId; } public void setUserId(String v) { userId = v; }
    public String getReason() { return reason; } public void setReason(String v) { reason = v; }
    public int getQuantity() { return quantity; } public void setQuantity(int v) { quantity = v; }
    public String getNote() { return note; } public void setNote(String v) { note = v; }
    public String getPhotoUrl() { return photoUrl; } public void setPhotoUrl(String v) { photoUrl = v; }
    public String getCreatedAt() { return createdAt; } public void setCreatedAt(String v) { createdAt = v; }
    public String getStatus() { return status; } public void setStatus(String v) { status = v; }
}
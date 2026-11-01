package com.mottainai.operacional.models;

import com.google.gson.annotations.SerializedName;

/**
 * Payload POST /api/v1/damages.
 * Campos enviados: productId, reason, quantity, note, photoUrl.
 * Store and employee identity are derived by the authenticated API.
 */
public class DamageRequest {
    @SerializedName("productId") private Integer productId;
    @SerializedName("reason") private String reason;
    @SerializedName("quantity") private int quantity;
    @SerializedName("note") private String note;
    @SerializedName("photoUrl") private String photoUrl;

    public DamageRequest() {}
    public DamageRequest(String productId, String reason, int quantity, String note, String photoUrl) {
        try { this.productId = Integer.valueOf(productId); } catch (Exception ignored) { this.productId = null; }
        this.reason = reason; this.quantity = quantity; this.note = note; this.photoUrl = photoUrl;
    }
    public Integer getProductId() { return productId; } public void setProductId(Integer v) { productId = v; }
    public String getReason() { return reason; } public void setReason(String v) { reason = v; }
    public int getQuantity() { return quantity; } public void setQuantity(int v) { quantity = v; }
    public String getNote() { return note; } public void setNote(String v) { note = v; }
    public String getPhotoUrl() { return photoUrl; } public void setPhotoUrl(String v) { photoUrl = v; }
}

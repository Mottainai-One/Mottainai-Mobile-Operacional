package com.mottainai.operacional.models;

import com.google.gson.annotations.SerializedName;

/**
 * Payload POST /api/v1/damages (contrato pendente - confirmar com backend).
 * Campos enviados: productId, reason, quantity, note, photoUrl.
 * storeId/userId derivados do Firebase ID token no backend, não digitados.
 */
public class DamageRequest {
    @SerializedName("productId") private String productId;
    @SerializedName("reason") private String reason;
    @SerializedName("quantity") private int quantity;
    @SerializedName("note") private String note;
    @SerializedName("photoUrl") private String photoUrl;

    public DamageRequest() {}
    public DamageRequest(String productId, String reason, int quantity, String note, String photoUrl) {
        this.productId = productId; this.reason = reason; this.quantity = quantity; this.note = note; this.photoUrl = photoUrl;
    }
    public String getProductId() { return productId; } public void setProductId(String v) { productId = v; }
    public String getReason() { return reason; } public void setReason(String v) { reason = v; }
    public int getQuantity() { return quantity; } public void setQuantity(int v) { quantity = v; }
    public String getNote() { return note; } public void setNote(String v) { note = v; }
    public String getPhotoUrl() { return photoUrl; } public void setPhotoUrl(String v) { photoUrl = v; }
}
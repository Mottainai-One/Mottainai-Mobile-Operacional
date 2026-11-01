package com.mottainai.operacional.models;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.Timestamp;

public class Suggestion {
    private String id;
    private String title;
    private String description;
    private String status;
    private Double currentPrice;
    private Double suggestedDiscount;
    private String storeId;
    private long createdAt;

    public Suggestion() {}

    public static Suggestion fromSnapshot(DocumentSnapshot doc) {
        Suggestion s = new Suggestion();
        s.id = doc.getId();
        s.title = doc.getString("title");
        s.description = doc.getString("description");
        s.status = doc.getString("status");
        s.currentPrice = firstNumber(doc, "currentPrice", "price", "productPrice");
        s.suggestedDiscount = firstNumber(doc, "suggestedDiscount", "discountPercent");
        s.storeId = doc.getString("storeID");
        Timestamp ts = doc.getTimestamp("createdAt");
        s.createdAt = ts != null ? ts.getSeconds() * 1000 : System.currentTimeMillis();
        return s;
    }

    public static Suggestion fromApi(ApiSuggestionResponse dto) {
        if (dto == null) return null;
        Suggestion suggestion = new Suggestion();
        suggestion.id = dto.id == null ? null : String.valueOf(dto.id);
        suggestion.title = dto.actionType == null ? "Sugestão" : dto.actionType.replace('_', ' ');
        suggestion.description = dto.description;
        suggestion.status = dto.status == null ? null : dto.status.toLowerCase(java.util.Locale.ROOT);
        suggestion.storeId = dto.alertId == null ? null : String.valueOf(dto.alertId);
        suggestion.createdAt = Alert.parseApiDate(dto.generatedAt);
        return suggestion;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public Double getCurrentPrice() { return currentPrice; }
    public Double getSuggestedDiscount() { return suggestedDiscount; }
    public String getStoreId() { return storeId; }
    public long getCreatedAt() { return createdAt; }

    private static Double firstNumber(DocumentSnapshot doc, String... fields) {
        for (String field : fields) {
            Double value = doc.getDouble(field);
            if (value != null) return value;
        }
        return null;
    }
}

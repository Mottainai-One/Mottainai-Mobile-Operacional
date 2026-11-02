package com.mottainai.operacional.models;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.Timestamp;

public class Alert {
    private String id;
    private String title;
    private String message;
    private String severity;
    private String storeId;
    private int days;
    private long createdAt;

    public Alert() {}

    public static Alert fromSnapshot(DocumentSnapshot doc) {
        Alert a = new Alert();
        a.id = doc.getId();
        a.title = doc.getString("title");
        a.message = doc.getString("message");
        a.severity = doc.getString("severity");
a.storeId = doc.getString("storeID");
        Long d = doc.getLong("days");
        a.days = d != null ? d.intValue() : 0;
        Timestamp ts = doc.getTimestamp("createdAt");
        a.createdAt = ts != null ? ts.getSeconds() * 1000 : System.currentTimeMillis();
        return a;
    }

    public static Alert fromApi(ApiAlertResponse dto) {
        if (dto == null) return null;
        Alert alert = new Alert();
        alert.id = dto.id == null ? null : String.valueOf(dto.id);
        alert.title = dto.title;
        alert.message = dto.description;
        String priority = dto.priority == null ? "" : dto.priority.toUpperCase(java.util.Locale.ROOT);
        alert.severity = priority.contains("CRIT") ? "CRITICO"
                : priority.equals("HIGH") || priority.equals("ALTA") ? "ATENCAO" : "MONITOR";
        alert.storeId = dto.storeId == null ? null : String.valueOf(dto.storeId);
        alert.createdAt = parseApiDate(dto.generatedAt);
        long age = System.currentTimeMillis() - alert.createdAt;
        alert.days = Math.max(0, (int) (age / 86_400_000L));
        return alert;
    }

    static long parseApiDate(String value) {
        try { return java.time.LocalDateTime.parse(value).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(); }
        catch (Exception ignored) { return System.currentTimeMillis(); }
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getSeverity() { return severity; }
    public String getStoreId() { return storeId; }
    public int getDays() { return days; }
    public long getCreatedAt() { return createdAt; }
}

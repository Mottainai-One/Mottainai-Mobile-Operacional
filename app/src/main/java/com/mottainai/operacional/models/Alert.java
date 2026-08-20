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

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getSeverity() { return severity; }
    public String getStoreId() { return storeId; }
    public int getDays() { return days; }
    public long getCreatedAt() { return createdAt; }
}
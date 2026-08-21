package com.mottainai.operacional.models;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.Timestamp;

public class Suggestion {
    private String id;
    private String title;
    private String description;
    private String status;
    private String storeId;
    private long createdAt;

    public Suggestion() {}

    public static Suggestion fromSnapshot(DocumentSnapshot doc) {
        Suggestion s = new Suggestion();
        s.id = doc.getId();
        s.title = doc.getString("title");
        s.description = doc.getString("description");
        s.status = doc.getString("status");
s.storeId = doc.getString("storeID");
        Timestamp ts = doc.getTimestamp("createdAt");
        s.createdAt = ts != null ? ts.getSeconds() * 1000 : System.currentTimeMillis();
        return s;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getStoreId() { return storeId; }
    public long getCreatedAt() { return createdAt; }
}
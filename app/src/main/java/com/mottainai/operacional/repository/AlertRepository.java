package com.mottainai.operacional.repository;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import com.mottainai.operacional.models.Alert;

import java.util.ArrayList;
import java.util.List;

public class AlertRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface AlertCallback {
        void onSuccess(List<Alert> alerts);
        void onError(Exception e);
    }

    public ListenerRegistration listenAlerts(String storeId, int days, AlertCallback callback) {
        return db.collection("alerts")
.whereEqualTo("storeID", storeId)
                .whereLessThanOrEqualTo("days", days)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        callback.onError(error);
                        return;
                    }
                    List<Alert> alerts = new ArrayList<>();
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Alert alert = Alert.fromSnapshot(doc);
                            if (alert != null) {
                                alerts.add(alert);
                            }
                        }
                    }
                    callback.onSuccess(alerts);
                });
    }
}
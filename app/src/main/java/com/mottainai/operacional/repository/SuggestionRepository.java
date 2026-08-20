package com.mottainai.operacional.repository;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import com.mottainai.operacional.models.Suggestion;

import java.util.ArrayList;
import java.util.List;

public class SuggestionRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface SuggestionCallback {
        void onSuccess(List<Suggestion> suggestions);
        void onError(Exception e);
    }

    public ListenerRegistration listenSuggestions(String storeId, SuggestionCallback callback) {
        return db.collection("suggestions")
                .whereEqualTo("storeID", storeId)
                .whereEqualTo("status", "pending")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        callback.onError(error);
                        return;
                    }
                    List<Suggestion> suggestions = new ArrayList<>();
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Suggestion suggestion = Suggestion.fromSnapshot(doc);
                            if (suggestion != null) {
                                suggestions.add(suggestion);
                            }
                        }
                    }
                    callback.onSuccess(suggestions);
                });
    }
}
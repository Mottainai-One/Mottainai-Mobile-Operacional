package com.mottainai.operacional.repository;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import com.mottainai.operacional.models.User;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface UserCallback {
        void onSuccess(User user);
        void onError(Exception e);
        void onNotFound();
    }

    public interface TeamCallback {
        void onSuccess(List<User> users);
        void onError(Exception error);
    }

    public void listTeam(String storeId, TeamCallback callback) {
        if (storeId == null || storeId.trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("Loja não identificada"));
            return;
        }
        db.collection("users").whereEqualTo("storeID", storeId).get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        callback.onError(task.getException());
                        return;
                    }
                    List<User> members = new ArrayList<>();
                    QuerySnapshot snapshot = task.getResult();
                    if (snapshot != null) {
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            members.add(new User(doc.getId(), doc.getString("name"),
                                    doc.getString("role"), doc.getString("storeID")));
                        }
                    }
                    callback.onSuccess(members);
                });
    }

    public void getUserProfile(String uid, UserCallback callback) {
        db.collection("users").document(uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.isSuccessful()) {
                            callback.onError(task.getException());
                            return;
                        }

                        DocumentSnapshot doc = task.getResult();
                        if (doc == null || !doc.exists()) {
                            callback.onNotFound();
                            return;
                        }

                        String name = doc.getString("name");
                        String role = doc.getString("role");
                        String storeId = doc.getString("storeID");

                        User user = new User(uid, name, role, storeId);
                        callback.onSuccess(user);
                    }
                });
    }
}

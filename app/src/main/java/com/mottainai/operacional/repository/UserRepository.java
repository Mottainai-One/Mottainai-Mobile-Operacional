package com.mottainai.operacional.repository;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.mottainai.operacional.models.User;

public class UserRepository {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface UserCallback {
        void onSuccess(User user);
        void onError(Exception e);
        void onNotFound();
    }

    // Busca o perfil e traduz DocumentSnapshot -> User
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
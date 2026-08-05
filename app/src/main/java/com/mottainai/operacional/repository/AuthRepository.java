package com.mottainai.operacional.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthRepository {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public interface AuthCallback {
        void onSuccess(String uid);
        void onError(Exception e);
    }

    // Realiza o login
    public void login(String email, String password, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        callback.onSuccess(uid);      // devolve o UID
                    } else {
                        callback.onError(task.getException());  // avisa o erro
                    }
                });
    }
}
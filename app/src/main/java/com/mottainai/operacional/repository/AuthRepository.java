package com.mottainai.operacional.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.mottainai.operacional.utils.SessionManager;

public class AuthRepository {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public interface AuthCallback {
        void onSuccess(String uid);
        void onError(Exception e);
    }

    public void login(String email, String password, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        callback.onSuccess(uid);
                    } else {
                        callback.onError(task.getException());
                    }
                });
    }

    /** Signs out locally even when the device is offline. */
    public void logout(SessionManager sessionManager) {
        mAuth.signOut();
        sessionManager.clearSession();
    }
}

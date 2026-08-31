package com.mottainai.operacional.storage;

import android.content.Context;
import android.net.Uri;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mottainai.operacional.utils.Constants;

import java.util.UUID;

/**
 * Upload de foto. Usa Firebase Storage quando configurado; caso contrário mocka URL.
 * Caminho: damages/{storeId}/{productId}/{uuid}.jpg
 */
public class PhotoStorageRepository {

    public interface PhotoCallback {
        void onSuccess(String photoUrl);
        void onError(String message);
    }

    public void uploadPhoto(Context context, Uri uri, String storeId, String productId, PhotoCallback callback) {
        if (uri == null) { callback.onSuccess(null); return; }
        if (Constants.USE_MOCK_DAMAGE) {
            // Mock: simula upload e retorna url fake
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                String fakeUrl = "https://mock.storage/damages/" + storeId + "/" + productId + "/" + UUID.randomUUID() + ".jpg";
                callback.onSuccess(fakeUrl);
            }, 800);
            return;
        }
        try {
            String path = "damages/" + storeId + "/" + productId + "/" + UUID.randomUUID() + ".jpg";
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(path);
            ref.putFile(uri)
               .addOnSuccessListener(task -> ref.getDownloadUrl()
                       .addOnSuccessListener(downloadUri -> callback.onSuccess(downloadUri.toString()))
                       .addOnFailureListener(e -> callback.onError("Falha ao obter URL: " + e.getMessage())))
               .addOnFailureListener(e -> callback.onError("Falha no upload: " + e.getMessage()));
        } catch (Exception e) {
            callback.onError("Erro no upload: " + e.getMessage());
        }
    }
}
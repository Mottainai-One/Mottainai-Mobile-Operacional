package com.mottainai.operacional.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mottainai.operacional.utils.SessionManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/** Mantém a deduplicação de notificações e registra tokens FCM no escopo do usuário autenticado. */
public class NotificationRepository {

    private static final String TAG = "MottainaiNotifications";

    private static final String PREFS_NAME = "mottainai_notifications";
    private static final String KEY_RECENT_NOTIFICATION_IDS = "recentNotificationIds";
    private static final int MAX_RECENT_NOTIFICATION_IDS = 50;

    private final SessionManager sessionManager;
    private final SharedPreferences preferences;

    public NotificationRepository(Context context) {
        Context appContext = context.getApplicationContext();
        sessionManager = new SessionManager(appContext);
        preferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void captureCurrentDeviceToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null) return;
            saveDeviceToken(task.getResult());
        });
    }

    public void saveDeviceToken(String token) {
        sessionManager.saveFcmToken(token);
        syncDeviceTokenToFirestore();
    }

    public void syncDeviceTokenToFirestore() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String token = sessionManager.getFcmToken();
        if (user == null || token == null || token.trim().isEmpty()) return;

        user.getIdToken(false).addOnSuccessListener(result -> {
            Map<String, Object> claims = result.getClaims();
            Object storeClaim = claims.get("storeID");
            Object roleClaim = claims.get("role");
            if (storeClaim == null || roleClaim == null) return;

            Map<String, Object> device = new HashMap<>();
            device.put("ownerUid", user.getUid());
            device.put("storeID", storeClaim);
            device.put("storeKey", String.valueOf(storeClaim));
            device.put("role", String.valueOf(roleClaim).trim().toUpperCase(Locale.ROOT));
            device.put("fcmToken", token);
            device.put("updatedAt", FieldValue.serverTimestamp());

            FirebaseFirestore.getInstance().collection("posNotificationDevices")
                    .document(deviceId(user.getUid(), token))
                    .set(device)
                    .addOnSuccessListener(unused -> sessionManager.markFcmTokenSynced())
                    .addOnFailureListener(error -> Log.w(TAG, "Não foi possível sincronizar o dispositivo FCM."));
        }).addOnFailureListener(error -> Log.w(TAG, "Não foi possível validar as permissões do dispositivo FCM."));
    }

    public void clearForLogout() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String token = sessionManager.getFcmToken();
        if (user != null && token != null && !token.trim().isEmpty()) {
            FirebaseFirestore.getInstance().collection("posNotificationDevices")
                    .document(deviceId(user.getUid(), token)).delete();
        }
        sessionManager.clearFcmToken();
        preferences.edit().clear().apply();
    }

    private static String deviceId(String uid, String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((uid + ":" + token).getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(digest.length * 2);
            for (byte value : digest) result.append(String.format(Locale.ROOT, "%02x", value));
            return result.toString();
        } catch (Exception error) {
            throw new IllegalStateException("SHA-256 indisponível para a chave do dispositivo.", error);
        }
    }

    /** Returns false when this notification was already displayed on this device. */
    public boolean registerNotificationIfNew(String notificationId) {
        if (notificationId == null || notificationId.trim().isEmpty()) return true;

        Set<String> storedIds = preferences.getStringSet(KEY_RECENT_NOTIFICATION_IDS, null);
        LinkedHashSet<String> recentIds = storedIds == null
                ? new LinkedHashSet<>()
                : new LinkedHashSet<>(storedIds);
        if (recentIds.contains(notificationId)) return false;

        recentIds.add(notificationId);
        while (recentIds.size() > MAX_RECENT_NOTIFICATION_IDS) {
            Iterator<String> iterator = recentIds.iterator();
            iterator.next();
            iterator.remove();
        }
        preferences.edit().putStringSet(KEY_RECENT_NOTIFICATION_IDS, recentIds).apply();
        return true;
    }
}

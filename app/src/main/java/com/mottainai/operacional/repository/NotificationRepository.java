package com.mottainai.operacional.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.messaging.FirebaseMessaging;
import com.mottainai.operacional.utils.SessionManager;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Mantém o token FCM e a deduplicação local. A sincronização remota só deve ser
 * adicionada quando a API oficial de dispositivos estiver disponível.
 */
public class NotificationRepository {

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
    }

    public void clearForLogout() {
        sessionManager.clearFcmToken();
        preferences.edit().clear().apply();
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

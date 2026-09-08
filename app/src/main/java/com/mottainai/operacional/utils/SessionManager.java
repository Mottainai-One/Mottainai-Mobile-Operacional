package com.mottainai.operacional.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.mottainai.operacional.models.User;
public class SessionManager {

    private static final String PREFS_NAME = "mottainai_session";
    private static final String KEY_UID = "uid";
    private static final String KEY_NAME = "name";
    private static final String KEY_ROLE = "role";
    private static final String KEY_STORE_ID = "storeId";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_AI_CHAT_SESSION_ID = "aiChatSessionId";
    private static final String KEY_FCM_TOKEN = "fcmToken";
    private static final String KEY_FCM_TOKEN_PENDING_SYNC = "fcmTokenPendingSync";
    private static final String KEY_NOTIFICATION_PERMISSION_PROMPTED = "notificationPermissionPrompted";
    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(User user) {
        prefs.edit()
                .putString(KEY_UID, user.getUid())
                .putString(KEY_NAME, user.getName())
                .putString(KEY_ROLE, user.getRole())
                .putString(KEY_STORE_ID, user.getStoreId())
                .apply();
    }

    public String getUid() {
        return prefs.getString(KEY_UID, null);
    }

    public String getName() {
        return prefs.getString(KEY_NAME, null);
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, null);
    }

    public String getStoreId() {
        return prefs.getString(KEY_STORE_ID, null);
    }

    public boolean isLoggedIn() {
        return getUid() != null;
    }

    public boolean hasCompleteProfile() {
        return getUid() != null
                && getStoreId() != null && !getStoreId().isEmpty()
                && getRole() != null && !getRole().isEmpty();
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getAiChatSessionId() {
        return prefs.getString(KEY_AI_CHAT_SESSION_ID, null);
    }

    public void saveAiChatSessionId(String sessionId) {
        prefs.edit().putString(KEY_AI_CHAT_SESSION_ID, sessionId).apply();
    }

    public String getFcmToken() {
        return prefs.getString(KEY_FCM_TOKEN, null);
    }

    /**
     * Um token novo sempre fica pendente até o endpoint oficial de dispositivos
     * confirmar o vínculo com o usuário autenticado.
     */
    public void saveFcmToken(String token) {
        if (token == null || token.trim().isEmpty()) return;

        boolean tokenChanged = !token.equals(getFcmToken());
        SharedPreferences.Editor editor = prefs.edit().putString(KEY_FCM_TOKEN, token);
        if (tokenChanged) {
            editor.putBoolean(KEY_FCM_TOKEN_PENDING_SYNC, true);
        }
        editor.apply();
    }

    public boolean isFcmTokenSyncPending() {
        return prefs.getBoolean(KEY_FCM_TOKEN_PENDING_SYNC, false);
    }

    public void markFcmTokenSynced() {
        prefs.edit().putBoolean(KEY_FCM_TOKEN_PENDING_SYNC, false).apply();
    }

    public void clearFcmToken() {
        prefs.edit()
                .remove(KEY_FCM_TOKEN)
                .remove(KEY_FCM_TOKEN_PENDING_SYNC)
                .apply();
    }

    public boolean hasNotificationPermissionPrompted() {
        return prefs.getBoolean(KEY_NOTIFICATION_PERMISSION_PROMPTED, false);
    }

    public void markNotificationPermissionPrompted() {
        prefs.edit().putBoolean(KEY_NOTIFICATION_PERMISSION_PROMPTED, true).apply();
    }
}

package com.mottainai.operacional.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREFS_NAME = "mottainai_session";
    private static final String KEY_UID = "uid";
    private static final String KEY_ROLE = "role";
    private static final String KEY_STORE_ID = "storeId";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String uid, String role, String storeId) {
        prefs.edit()
                .putString(KEY_UID, uid)
                .putString(KEY_ROLE, role)
                .putString(KEY_STORE_ID, storeId)
                .apply();
    }

    public String getUid()    { return prefs.getString(KEY_UID, null); }
    public String getRole()   { return prefs.getString(KEY_ROLE, null); }
    public String getStoreId(){ return prefs.getString(KEY_STORE_ID, null); }

    public boolean isLoggedIn() { return getUid() != null; }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}

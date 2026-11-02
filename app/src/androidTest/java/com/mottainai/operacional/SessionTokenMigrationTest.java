package com.mottainai.operacional;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.mottainai.operacional.utils.SessionManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.UUID;
import static org.junit.Assert.*;

/** Isolated preferences only: never changes the actual device login. */
@RunWith(AndroidJUnit4.class)
public class SessionTokenMigrationTest {
    @Test public void legacyAndFirebaseTokensDoNotBecomeOperationalTokens() {
        Context base = InstrumentationRegistry.getInstrumentation().getTargetContext();
        String preferenceName = "migration-test-" + UUID.randomUUID();
        Context isolated = new ContextWrapper(base) {
            @Override public SharedPreferences getSharedPreferences(String name, int mode) {
                return base.getSharedPreferences(preferenceName, mode);
            }
        };
        try {
            SharedPreferences preferences = isolated.getSharedPreferences("ignored", Context.MODE_PRIVATE);
            preferences.edit().putString("token", "legacy-test-token").commit();
            SessionManager session = new SessionManager(isolated);
            assertNull(session.getApiAccessToken());
            assertNull(session.getApiRefreshToken());
            session.saveFirebaseToken("firebase-test-token");
            assertNull(session.getApiAccessToken());
            session.saveApiAccessToken("operational-test-token");
            session.saveApiRefreshToken("refresh-test-token");
            assertEquals("operational-test-token", session.getApiAccessToken());
            assertEquals("refresh-test-token", session.getApiRefreshToken());
            assertFalse(preferences.contains("token"));
        } finally {
            base.deleteSharedPreferences(preferenceName);
        }
    }
}

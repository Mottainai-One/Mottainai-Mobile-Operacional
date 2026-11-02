package com.mottainai.operacional;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.os.SystemClock;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.firebase.auth.FirebaseAuth;
import com.mottainai.operacional.activities.LoginActivity;
import com.mottainai.operacional.utils.SessionManager;

import org.junit.Test;
import org.junit.Assume;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DeviceOperationalRoleLoginTest {

    @Test
    public void realFirebaseAccountCompletesOperationalLogin() throws Exception {
        var args = InstrumentationRegistry.getArguments();
        String email = args.getString("email");
        String password = args.getString("password");
        String expectedRole = args.getString("role");
        Assume.assumeTrue("Passe email, password e role pelo instrumentation runner.",
                email != null && password != null && expectedRole != null);

        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SessionManager session = new SessionManager(context);
        session.clearSession();
        FirebaseAuth.getInstance().signOut();

        try (ActivityScenario<LoginActivity> ignored = ActivityScenario.launch(LoginActivity.class)) {
            onView(withId(R.id.et_email)).perform(replaceText(email));
            onView(withId(R.id.et_password)).perform(replaceText(password), closeSoftKeyboard());
            onView(withId(R.id.btn_login)).perform(click());

            long deadline = SystemClock.uptimeMillis() + 180_000L;
            while (SystemClock.uptimeMillis() < deadline && !session.hasCompleteProfile()) {
                InstrumentationRegistry.getInstrumentation().waitForIdleSync();
                SystemClock.sleep(500L);
            }
        }

        assertTrue("O login não concluiu nem salvou o perfil de operação.", session.hasCompleteProfile());
        assertEquals(expectedRole, session.getRole());
        assertNotNull(session.getToken());
        assertTrue("O token Firebase salvo está vazio.", !session.getToken().isEmpty());
    }
}

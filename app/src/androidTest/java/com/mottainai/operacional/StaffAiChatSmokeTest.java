package com.mottainai.operacional;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.firebase.auth.FirebaseAuth;
import com.mottainai.operacional.activities.LoginActivity;
import com.mottainai.operacional.utils.SessionManager;

import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

/** Opt-in real login/chat check. Credentials are supplied at execution, never in source. */
@RunWith(AndroidJUnit4.class)
public class StaffAiChatSmokeTest {
    @Test
    public void staffCanLoginAndReceiveGroundedAiReply() throws Exception {
        Bundle args = InstrumentationRegistry.getArguments();
        String email = args.getString("testEmail", "");
        String password = args.getString("testPassword", "");
        String role = args.getString("testRole", "");
        Assume.assumeTrue(!email.isEmpty() && !password.isEmpty() && !role.isEmpty());
        String question = new String(Base64.decode(args.getString("questionBase64", ""),
                Base64.DEFAULT), StandardCharsets.UTF_8);
        assertTrue(!question.trim().isEmpty());
        assertTrue(!args.getString("expectedNumbers", "").isEmpty());
        String[] expected = args.getString("expectedNumbers", "").split(",");
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SessionManager session = new SessionManager(context);
        FirebaseAuth.getInstance().signOut();
        session.clearSession();
        Intent login = new Intent(context, LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(login)) {
            onView(withId(R.id.et_email)).perform(replaceText(email));
            onView(withId(R.id.et_password)).perform(replaceText(password), closeSoftKeyboard());
            onView(withId(R.id.btn_login)).perform(click());
            await(() -> assertEquals(role, session.getRole()), 90_000);
            await(() -> onView(withId(R.id.iaAlertsFragment)).perform(click()), 60_000);
            onView(withText("Chat")).perform(click());
            await(() -> onView(withId(R.id.btn_chat_send)).check(matches(isEnabled())), 60_000);
            onView(withId(R.id.et_chat_message)).perform(replaceText(question), closeSoftKeyboard());
            onView(withId(R.id.btn_chat_send)).perform(click());
            String[] reply = {""};
            await(() -> onView(withId(R.id.rv_chat_messages)).check((view, error) -> {
                if (error != null) throw error;
                RecyclerView list = (RecyclerView) view;
                assertNotNull(list.getAdapter());
                assertTrue(list.getAdapter().getItemCount() >= 2);
                RecyclerView.ViewHolder holder = list.findViewHolderForAdapterPosition(
                        list.getAdapter().getItemCount() - 1);
                assertNotNull(holder);
                TextView text = holder.itemView.findViewById(R.id.tv_chat_message);
                reply[0] = text.getText().toString();
                for (String value : expected) assertTrue(reply[0], reply[0].contains(value));
                assertTrue(!reply[0].contains("Não encontrei essa informação com segurança"));
            }), 150_000);
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            Thread.sleep(1000); // Allow the committed UI state to reach the screenshot surface.
            Bitmap screenshot = InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
            assertNotNull(screenshot);
            try (FileOutputStream output = new FileOutputStream(new File(
                    context.getExternalFilesDir(null), "ai-role-" + role + ".png"))) {
                screenshot.compress(Bitmap.CompressFormat.PNG, 100, output);
            }
            try (FileOutputStream output = new FileOutputStream(new File(
                    context.getExternalFilesDir(null), "ai-role-" + role + ".txt"))) {
                output.write(reply[0].getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    private static void await(Runnable assertion, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        Throwable last = null;
        do {
            try { assertion.run(); return; }
            catch (AssertionError | RuntimeException error) { last = error; }
            Thread.sleep(1000);
        } while (System.currentTimeMillis() < deadline);
        throw new AssertionError("Timed out waiting for the real app flow", last);
    }
}

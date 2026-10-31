package com.mottainai.operacional;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertTrue;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(AndroidJUnit4.class)
public class DeviceChatSmokeTest {

    @Test
    public void authenticatedChatReturnsAnAnswerAndCapturesProof() throws Exception {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            // Usa o mesmo atalho do cabeçalho que o usuário toca no app. Em tablets,
            // o item da navegação inferior pode não estar visível no viewport do Espresso.
            onView(withId(R.id.btn_assistant)).perform(click());

            waitUntil(scenario, activity -> {
                View send = activity.findViewById(R.id.btn_chat_send);
                RecyclerView messages = activity.findViewById(R.id.rv_chat_messages);
                return send != null && messages != null && send.isEnabled();
            }, 20_000L);

            AtomicInteger initialCount = new AtomicInteger();
            scenario.onActivity(activity -> {
                RecyclerView messages = activity.findViewById(R.id.rv_chat_messages);
                initialCount.set(messages == null || messages.getAdapter() == null
                        ? 0 : messages.getAdapter().getItemCount());
            });

            onView(withId(R.id.et_chat_message))
                    .perform(replaceText("Responda somente: IA operacional funcionando."), closeSoftKeyboard());
            onView(withId(R.id.btn_chat_send)).perform(click());

            waitUntil(scenario, activity -> {
                RecyclerView messages = activity.findViewById(R.id.rv_chat_messages);
                View progress = activity.findViewById(R.id.progress_chat);
                View error = activity.findViewById(R.id.tv_chat_error);
                if (messages == null || progress == null || error == null) return false;
                int count = messages.getAdapter() == null ? 0 : messages.getAdapter().getItemCount();
                return count >= initialCount.get() + 2
                        && progress.getVisibility() != View.VISIBLE
                        && error.getVisibility() != View.VISIBLE;
            }, 70_000L);

            AtomicBoolean proofSaved = new AtomicBoolean(false);
            scenario.onActivity(activity -> {
                View root = activity.getWindow().getDecorView().getRootView();
                Bitmap screenshot = Bitmap.createBitmap(
                        root.getWidth(), root.getHeight(), Bitmap.Config.ARGB_8888);
                root.draw(new Canvas(screenshot));
                File proof = new File(activity.getExternalFilesDir(null),
                        "mottainai-chat-success.png");
                try (FileOutputStream output = new FileOutputStream(proof)) {
                    proofSaved.set(screenshot.compress(Bitmap.CompressFormat.PNG, 100, output)
                            && proof.isFile() && proof.length() > 0);
                } catch (Exception ignored) {
                    proofSaved.set(false);
                } finally {
                    screenshot.recycle();
                }
            });
            assertTrue("O print da janela do Mottainai não foi salvo.", proofSaved.get());
        }
    }

    private static void waitUntil(ActivityScenario<MainActivity> scenario,
                                  ActivityCondition condition,
                                  long timeoutMs) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMs;
        AtomicBoolean satisfied = new AtomicBoolean(false);
        while (System.currentTimeMillis() < deadline && !satisfied.get()) {
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(activity -> satisfied.set(condition.matches(activity)));
            if (!satisfied.get()) Thread.sleep(1_000L);
        }
        assertTrue("A interface não atingiu o estado esperado dentro do prazo.", satisfied.get());
    }

    private interface ActivityCondition {
        boolean matches(MainActivity activity);
    }
}

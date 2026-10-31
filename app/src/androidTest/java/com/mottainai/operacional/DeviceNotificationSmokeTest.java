package com.mottainai.operacional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.service.notification.StatusBarNotification;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.RemoteMessage;
import com.mottainai.operacional.notifications.NotificationChannelManager;
import com.mottainai.operacional.notifications.NotificationRouter;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

@RunWith(AndroidJUnit4.class)
public class DeviceNotificationSmokeTest {

    @Test
    public void fcmPayloadRoutesToTheOperationalAlertChannel() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.cancelAll();

        NotificationChannelManager.ensureChannels(context);
        assertTrue("A sessão local precisa estar autenticada para rotear o alerta.",
                new com.mottainai.operacional.utils.SessionManager(context).hasCompleteProfile());
        assertTrue("O Android precisa permitir notificações neste aparelho.",
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                        || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED);

        String notificationId = "qa-alert-router-" + UUID.randomUUID();
        String title = "Teste operacional Mottainai";
        String body = "Roteamento de alerta funcionando no aparelho.";
        RemoteMessage message = new RemoteMessage.Builder("qa@mottainai.local")
                .setMessageId(notificationId)
                .addData("notificationType", "alert")
                .addData("notificationId", notificationId)
                .addData("title", title)
                .addData("body", body)
                .build();

        new NotificationRouter(context).show(message);

        boolean found = false;
        for (StatusBarNotification active : manager.getActiveNotifications()) {
            Notification notification = active.getNotification();
            CharSequence activeTitle = notification.extras.getCharSequence(Notification.EXTRA_TITLE);
            CharSequence activeBody = notification.extras.getCharSequence(Notification.EXTRA_TEXT);
            if (title.contentEquals(activeTitle)) {
                assertEquals(body, String.valueOf(activeBody));
                assertEquals(NotificationChannelManager.CHANNEL_ALERTS, notification.getChannelId());
                found = true;
                break;
            }
        }
        assertTrue("A notificação operacional não ficou ativa no Android.", found);
    }
}

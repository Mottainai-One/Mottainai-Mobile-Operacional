package com.mottainai.operacional.notifications;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.RemoteMessage;
import com.mottainai.operacional.MainActivity;
import com.mottainai.operacional.R;
import com.mottainai.operacional.repository.NotificationRepository;
import com.mottainai.operacional.utils.RoleHelper;
import com.mottainai.operacional.utils.SessionManager;

import java.util.Locale;
import java.util.Map;

/** Converts a validated FCM payload into one authorized in-app destination. */
public final class NotificationRouter {

    public static final String EXTRA_DESTINATION = "notification_destination";
    public static final String EXTRA_ENTITY_ID = "notification_entity_id";
    public static final String EXTRA_NOTIFICATION_ID = "notification_id";

    public enum Destination {
        HOME,
        PRODUCT,
        DAMAGE,
        ALERT,
        SUGGESTION,
        INVENTORY;

        public static Destination fromPayload(Map<String, String> data) {
            String value = firstNonBlank(data.get("notificationType"), data.get("type"), data.get("route"));
            if (value == null) return HOME;

            switch (value.trim().toLowerCase(Locale.ROOT)) {
                case "product":
                case "products":
                    return PRODUCT;
                case "damage":
                case "damages":
                case "avaria":
                    return DAMAGE;
                case "alert":
                case "alerts":
                case "ia/alerts":
                    return ALERT;
                case "suggestion":
                case "suggestions":
                case "ia/suggestions":
                    return SUGGESTION;
                case "inventory":
                case "inventario":
                    return INVENTORY;
                default:
                    return HOME;
            }
        }
    }

    private final Context context;
    private final SessionManager sessionManager;
    private final NotificationRepository notificationRepository;

    public NotificationRouter(Context context) {
        this.context = context.getApplicationContext();
        sessionManager = new SessionManager(this.context);
        notificationRepository = new NotificationRepository(this.context);
    }

    public void show(RemoteMessage message) {
        if (!sessionManager.hasCompleteProfile() || !canPostNotifications()) return;

        Map<String, String> data = message.getData();
        Destination destination = Destination.fromPayload(data);
        if (!isDestinationAllowed(destination, sessionManager.getRole())) return;

        String notificationId = firstNonBlank(data.get("notificationId"), message.getMessageId());
        if (!notificationRepository.registerNotificationIfNew(notificationId)) return;

        NotificationChannelManager.ensureChannels(context);
        RemoteMessage.Notification remoteNotification = message.getNotification();
        String title = firstNonBlank(data.get("title"),
                remoteNotification == null ? null : remoteNotification.getTitle(),
                context.getString(R.string.notification_default_title));
        String body = firstNonBlank(data.get("body"),
                remoteNotification == null ? null : remoteNotification.getBody(),
                context.getString(R.string.notification_default_body));

        Intent intent = new Intent(context, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(EXTRA_DESTINATION, destination.name())
                .putExtra(EXTRA_ENTITY_ID, firstNonBlank(data.get("entityId"), data.get("id")))
                .putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        int pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationRequestCode(notificationId),
                intent, pendingIntentFlags);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context,
                NotificationChannelManager.channelFor(destination))
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(ContextCompat.getColor(context, R.color.primary_green))
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setCategory(NotificationCompat.CATEGORY_STATUS);

        NotificationManagerCompat.from(context)
                .notify(notificationRequestCode(notificationId), notification.build());
    }

    public static Destination destinationFromIntent(Intent intent) {
        if (intent == null) return null;
        String value = intent.getStringExtra(EXTRA_DESTINATION);
        if (value == null) return null;
        try {
            return Destination.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public static boolean isDestinationAllowed(Destination destination, String role) {
        if (role == null || role.trim().isEmpty()) return false;
        if (destination == Destination.SUGGESTION) {
            return RoleHelper.canViewSuggestions(role);
        }
        if (destination == Destination.INVENTORY) {
            return RoleHelper.canRegisterProduct(role);
        }
        return true;
    }

    private boolean canPostNotifications() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private static int notificationRequestCode(String notificationId) {
        return notificationId == null || notificationId.trim().isEmpty()
                ? (int) (System.currentTimeMillis() & 0x7fffffff)
                : notificationId.hashCode() & 0x7fffffff;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) return value;
        }
        return null;
    }
}

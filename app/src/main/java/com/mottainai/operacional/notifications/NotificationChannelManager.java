package com.mottainai.operacional.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.mottainai.operacional.R;

/** Creates the app notification channels once on Android 8+. */
public final class NotificationChannelManager {

    public static final String CHANNEL_ALERTS = "mottainai_alerts";
    public static final String CHANNEL_SUGGESTIONS = "mottainai_suggestions";
    public static final String CHANNEL_INVENTORY = "mottainai_inventory";
    public static final String CHANNEL_GENERAL = "mottainai_general";

    private NotificationChannelManager() {
    }

    public static void ensureChannels(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) return;

        manager.createNotificationChannel(createChannel(
                CHANNEL_ALERTS,
                context.getString(R.string.notification_channel_alerts_name),
                context.getString(R.string.notification_channel_alerts_description),
                NotificationManager.IMPORTANCE_HIGH));
        manager.createNotificationChannel(createChannel(
                CHANNEL_SUGGESTIONS,
                context.getString(R.string.notification_channel_suggestions_name),
                context.getString(R.string.notification_channel_suggestions_description),
                NotificationManager.IMPORTANCE_DEFAULT));
        manager.createNotificationChannel(createChannel(
                CHANNEL_INVENTORY,
                context.getString(R.string.notification_channel_inventory_name),
                context.getString(R.string.notification_channel_inventory_description),
                NotificationManager.IMPORTANCE_HIGH));
        manager.createNotificationChannel(createChannel(
                CHANNEL_GENERAL,
                context.getString(R.string.notification_channel_general_name),
                context.getString(R.string.notification_channel_general_description),
                NotificationManager.IMPORTANCE_DEFAULT));
    }

    public static String channelFor(NotificationRouter.Destination destination) {
        switch (destination) {
            case ALERT:
                return CHANNEL_ALERTS;
            case SUGGESTION:
                return CHANNEL_SUGGESTIONS;
            case INVENTORY:
                return CHANNEL_INVENTORY;
            default:
                return CHANNEL_GENERAL;
        }
    }

    private static NotificationChannel createChannel(String id, String name, String description, int importance) {
        NotificationChannel channel = new NotificationChannel(id, name, importance);
        channel.setDescription(description);
        return channel;
    }
}

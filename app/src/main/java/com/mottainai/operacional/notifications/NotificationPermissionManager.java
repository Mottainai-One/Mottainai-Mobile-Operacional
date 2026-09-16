package com.mottainai.operacional.notifications;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.mottainai.operacional.R;
import com.mottainai.operacional.utils.SessionManager;

/** Handles the Android 13+ notification permission without blocking app access. */
public final class NotificationPermissionManager {

    private static final int REQUEST_POST_NOTIFICATIONS = 204;

    private NotificationPermissionManager() {
    }

    public static void requestPermissionIfNeeded(AppCompatActivity activity, SessionManager sessionManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED
                || sessionManager.hasNotificationPermissionPrompted()) {
            return;
        }

        new AlertDialog.Builder(activity)
                .setTitle(R.string.notification_permission_title)
                .setMessage(R.string.notification_permission_message)
                .setPositiveButton(R.string.notification_permission_enable, (dialog, which) -> {
                    sessionManager.markNotificationPermissionPrompted();
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS},
                            REQUEST_POST_NOTIFICATIONS);
                })
                .setNegativeButton(R.string.notification_permission_not_now, (dialog, which) ->
                        sessionManager.markNotificationPermissionPrompted())
                .setOnCancelListener(dialog -> sessionManager.markNotificationPermissionPrompted())
                .show();
    }
}

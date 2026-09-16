package com.mottainai.operacional.notifications;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mottainai.operacional.repository.NotificationRepository;

/** Receives FCM token rotations and data notifications for the operational app. */
public class MottainaiFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        new NotificationRepository(this).saveDeviceToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        new NotificationRouter(this).show(message);
    }
}

package com.example.noticeboard;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseCloudMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FirebaseCloudMessagingService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Handle the incoming message here
        if (remoteMessage.getData().size() > 0) {
            String title = remoteMessage.getData().get("title");
            String message = remoteMessage.getData().get("message");
            showNotification(title, message);
        }
    }

    private void showNotification(String title, String message) {

    }

    @Override
    public void onNewToken(String token) {
        // Handle the device token here
        Log.d(TAG, "Refreshed token: " + token);
    }
}

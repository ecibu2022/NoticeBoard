package com.example.noticeboard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
    private MediaPlayer mediaPlayer;

    @Override
    public void onReceive(Context context, Intent intent) {

        mediaPlayer = MediaPlayer.create(context, R.raw.alarm_sound);

        // Check if the MediaPlayer is not null and not already playing
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            // Start playing the sound
            mediaPlayer.start();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Release the MediaPlayer
                    if (mediaPlayer != null) {
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                }
            }, 120000); // 120000 milliseconds = 2 minutes
        }

        Toast.makeText(context, "Event Alarm Triggered", Toast.LENGTH_SHORT).show();
    }
}

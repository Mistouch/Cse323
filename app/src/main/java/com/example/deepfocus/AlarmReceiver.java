package com.example.deepfocus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
    private static Ringtone ringtone;
    @Override
    public void onReceive(Context context, Intent intent) {
        // Trigger a notification or perform some action
        // Play default alarm sound
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        if (ringtone == null) { // Initialize ringtone if not already playing
            ringtone = RingtoneManager.getRingtone(context, alarmUri);
        }

        if (!ringtone.isPlaying()) { // Play the ringtone only if it's not already playing
            ringtone.play();
        }

        // Optionally, display a Toast
        Toast.makeText(context, "You Are Getting Distracted", Toast.LENGTH_SHORT).show();
    }
    public static void stopRingtone() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
            ringtone = null; // Reset the reference to allow the ringtone to be garbage collected
        }
    }
}

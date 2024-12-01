package com.example.deepfocus;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MainActivity extends AppCompatActivity {

    private Button startStopButton;
    private Button stepButton;
    private TextView sessionTimer;
    private long sessionStartTime;
    private long distractionTime = 0;  // Time spent on other apps (distractions)
    private long distractionStartTime = 0;
    boolean puffer =true;
    boolean AlarmOn=false;
    boolean cametoRe=false;

    boolean flag=false;
    private boolean isSessionActive = false;

    private static final int REQUEST_CODE_SCHEDULE_EXACT_ALARM = 123;
    private static final int POST_NOTIFICATION_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startStopButton = findViewById(R.id.startStopButton);
        stepButton = findViewById(R.id.startStepCount);
        sessionTimer = findViewById(R.id.sessionTimer);
        createNotificationChannel();

        stepButton.setOnClickListener(v -> {
            //System.out.println("Going to counting");
            Intent intent = new Intent(MainActivity.this, Awasf.class);
            startActivity(intent);
        });
        startStopButton.setOnClickListener(v -> setFlag());
        // Check if the app has usage stats permission

        // Check and request notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        POST_NOTIFICATION_REQUEST_CODE);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        // App has come to the foreground
        if(AlarmOn) {
            cancelAlarm();
        }
        puffer=true;
        cancelAlarm();
        System.out.println("AppState: App is in the foreground");
        System.out.println(distractionTime);
        if(distractionStartTime!=0){
            distractionTime+=(System.currentTimeMillis()-distractionStartTime);
        }
        System.out.println(distractionTime);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // App has gone to the background
        System.out.println("AppState: App is in the background");
        puffer=false;

        if(flag) {
            distractionStartTime=System.currentTimeMillis();
            long currTime=System.currentTimeMillis();

            // Schedule the exact alarm
            scheduleExactAlarm();
        }
    }

    public void setFlag(){
        if(!flag) {
            flag = true;
            startStopButton.setText("End Study Session");
            sessionTimer.setText("Session started...");
            sessionStartTime=System.currentTimeMillis();
        }
        else {
            flag=false;
            startStopButton.setText("Start Study Session");
            sessionTimer.setText("No Session");

            long endSession=System.currentTimeMillis();

            long studyTime=endSession-distractionTime-sessionStartTime;
            sessionStartTime=0;
            distractionTime=0;

            sessionTimer.setText("Study Time: " + (studyTime / 1000) + " seconds");

        }

    }
    @SuppressLint("ScheduleExactAlarm")
    private void scheduleExactAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerTime = SystemClock.elapsedRealtime() + 10 * 1000; // 10 seconds from now

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, alarmIntent);
        } else {
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, alarmIntent);
        }
        AlarmOn=true;

        System.out.println("Exact alarm set for 10 seconds later.");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "MyChannel";
            String description = "Channel for notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("MY_CHANNEL_ID", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNotification() {
        // Create an Intent for the activity to open when the notification is tapped
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Add FLAG_IMMUTABLE to the PendingIntent
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "MY_CHANNEL_ID")
                .setSmallIcon(R.drawable.ic_notification) // Replace with your drawable resource
                .setContentTitle("Pufferfish Notification")
                .setContentText("You are getting Distracted!! Go to Study!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(1, builder.build());
    }
    private void cancelAlarm() {

        AlarmReceiver.stopRingtone();
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Create the same PendingIntent used to schedule the alarm
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Cancel the alarm
        alarmManager.cancel(alarmIntent);

        // Optionally show a toast
        Toast.makeText(this, "Good Work!", Toast.LENGTH_SHORT).show();
        AlarmOn=false;
    }



}

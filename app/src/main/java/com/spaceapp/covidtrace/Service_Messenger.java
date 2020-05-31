package com.spaceapp.covidtrace;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

import java.util.Calendar;

public class Service_Messenger extends Service {

    public static final String CHANNEL_ID = "channel_messenger_service";
    public static final String MyPREFERENCES = "MyPrefs";
    public static final String Name = "nameKey";
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // Nearby.getMessagesClient(Service_Messenger.this).publish(mMessage);
        }
    };
    private MessageListener mMessageListener;

    //Alarm alarm = new Alarm();
    private Message mMessage;
    private SharedPreferences sharedPreferences;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Covid Tracing Service",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            assert manager != null;
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Covid Tracing Service")
                .setSmallIcon(R.drawable.ic_virus)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
    }

    @Override
    public void onCreate() {

        createNotificationChannel();
        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        mMessageListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                Log.d("HEyyyLOOO", "Found message: " + new String(message.getContent()));

                String s1 = sharedPreferences.getString(Name, "");
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Name, Calendar.getInstance().getTime() + " Found" + "\n" + s1);
                editor.apply();
            }

            @Override
            public void onLost(Message message) {
                Log.d("HEyyyLOOO", "Lost sight of message: " + new String(message.getContent()));

                String s1 = sharedPreferences.getString(Name, "");
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Name, Calendar.getInstance().getTime() + " Lost" + "\n" + s1);
                editor.apply();
            }

        };
        mMessage = new Message("Hello Gu kha".getBytes());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Nearby.getMessagesClient(this).publish(mMessage);
        Nearby.getMessagesClient(this).subscribe(mMessageListener);
        createNotification();
        //   handler.postDelayed(runnable, 60*1000);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Nearby.getMessagesClient(this).unpublish(mMessage);
        Nearby.getMessagesClient(this).unsubscribe(mMessageListener);
        super.onDestroy();
    }
}


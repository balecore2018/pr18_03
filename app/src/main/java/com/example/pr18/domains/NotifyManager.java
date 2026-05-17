package com.example.pr18.domains;

import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.pr18.R;
import com.example.pr18.presentations.BasketActivity;

public class NotifyManager {

    private static final String CHANNEL_ID = "Matule_Channel";

    private final Context context;

    public NotifyManager(Context context) {
        this.context = context;
    }

    public void SendNotify(String message) {
        if (PermissionManager.CheckPermission(context) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        CreateNotificationChannel();

        Intent notifyIntent = new Intent(context, BasketActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                0,
                notifyIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.icon);
        builder.setContentTitle(context.getString(R.string.order_delivered_title));
        builder.setContentText(message);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setContentIntent(contentIntent);
        builder.setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(100, builder.build());
    }

    public void CreateNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Matule",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
        );

        android.app.NotificationManager notificationManager =
                context.getSystemService(android.app.NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }
}

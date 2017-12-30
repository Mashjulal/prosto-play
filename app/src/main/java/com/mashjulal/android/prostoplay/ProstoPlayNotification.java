package com.mashjulal.android.prostoplay;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

public class ProstoPlayNotification {

    private static final String NOTIFICATION_TAG = "ProstoPlay";
    public static final String CLASS_NAME = ProstoPlayNotification.class.getName();

    public static void notify(final Context context,
                              final String songName, final String artist, String actionType) {
        final Intent pauseIntent = new Intent(context, MusicPlayerService.class);
        final boolean isCanceled;

        final RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.notification);
        rv.setTextViewText(R.id.notification_tv_track_name, songName);
        rv.setTextViewText(R.id.notification_tv_artist_name, artist);

        if (actionType.equals("pause")) {
            pauseIntent.putExtra("pause", true);
            rv.setImageViewResource(R.id.notification_ib_play, R.drawable.ic_action_pause_black);
            isCanceled = false;
        }
        else {
            pauseIntent.putExtra("pause", false);
            rv.setImageViewResource(R.id.notification_ib_play, R.drawable.ic_action_play_black);
            isCanceled = true;
        }
        pauseIntent.putExtra("SENDER_NAME", CLASS_NAME);
        final PendingIntent pi = PendingIntent.getService(context, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.notification_ib_play, pi);

        Intent intent = new Intent(context, MainActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.layout_notification, pendingIntent);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setDefaults(Notification.PRIORITY_MAX)
                .setSmallIcon(R.drawable.ic_action_play)
                .setContent(rv)
                .setAutoCancel(isCanceled);

        notify(context, builder.build());
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private static void notify(final Context context, final Notification notification) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.notify(NOTIFICATION_TAG, 0, notification);
        } else {
            nm.notify(NOTIFICATION_TAG.hashCode(), notification);
        }
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public static void cancel(final Context context) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.cancel(NOTIFICATION_TAG, 0);
        } else {
            nm.cancel(NOTIFICATION_TAG.hashCode());
        }
    }
}

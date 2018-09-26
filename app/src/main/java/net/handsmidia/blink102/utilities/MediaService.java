package net.handsmidia.blink102;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.TextView;

import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;


public class MediaService extends Service {

    private MediaSession mSession;
    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_STOP = "action_stop";
    private MediaSessionManager mManager;
    private MediaController mController;
    Binder mBinder = new LocalBinder();
    private NotificationManager mNotificationManager;
    private String trackMusic = "Blink 102 FM";
    private String TAG_NOTIFICATION = "NOTIFICATION";

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        finishMedia();
        return super.onUnbind(intent);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void handleIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        String action = intent.getAction();

        if (action.equalsIgnoreCase(ACTION_PLAY)) {
            mController.getTransportControls().play();
        } else if (action.equalsIgnoreCase(ACTION_PAUSE)) {
            mController.getTransportControls().pause();
        } else if (action.equalsIgnoreCase(ACTION_STOP)) {
            mController.getTransportControls().stop();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    private Notification.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(getApplicationContext(), MediaService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new Notification.Action.Builder(icon, title, pendingIntent).build();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void buildNotification(Notification.Action action, String textNotification) {
        Notification.MediaStyle style = new Notification.MediaStyle();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_radio_black_24dp)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(textNotification)
                .setDeleteIntent(pendingIntent)
                .setOngoing(true)
                .setAutoCancel(true)
                .setStyle(style);

        builder.addAction(action);
        style.setShowActionsInCompactView(0);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(TAG_NOTIFICATION, 0, builder.build());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mManager == null) {
            initMediaSession();
        }
        handleIntent(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initMediaSession() {

        mSession = new MediaSession(getApplicationContext(), "example player session");
        mController = new MediaController(getApplicationContext(), mSession.getSessionToken());

        mSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE), getTrackMusic());
                doService(true, null);
            }

            @Override
            public void onPause() {
                super.onPause();
                buildNotification(generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY), getTrackMusic());
                doService(false, null);
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
            }

            @Override
            public void onFastForward() {
                super.onFastForward();
            }

            @Override
            public void onRewind() {
                super.onRewind();
            }

            @Override
            public void onStop() {
                super.onStop();
            }
        });
    }

    public class LocalBinder extends Binder {
        public MediaService getServerInstance() {
            return MediaService.this;
        }
    }

    public void playMedia() {
        buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE), getTrackMusic());
    }

    public void pauseMedia() {
        buildNotification(generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY), getTrackMusic());
    }

    public void finishMedia() {
        if (mNotificationManager != null) {
            mNotificationManager.cancelAll();
            mNotificationManager.cancel(TAG_NOTIFICATION, 0);
        }
        doService(false, "finish");
    }

    private void doService(boolean value, String finishApp) {
        Intent intent = new Intent("my-event");
        intent.putExtra("message", value);
        intent.putExtra("finish", finishApp);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public String getTrackMusic() {
        return trackMusic;
    }

    public void setTrackMusic(String trackMusic) {
        this.trackMusic = trackMusic;
        buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE), trackMusic);
    }
}

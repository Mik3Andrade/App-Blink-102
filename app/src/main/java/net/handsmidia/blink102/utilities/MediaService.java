package net.handsmidia.blink102.utilities;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.RemoteViews;

import net.handsmidia.blink102.R;
import net.handsmidia.blink102.view.MainActivity;


public class MediaService extends Service {

    private static final String CHANNEL_ID = "channel-01";
    private MediaSession mSession;
    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_STOP = "action_stop";

    private MediaController mController;
    Binder mBinder = new LocalBinder();
    private NotificationManager mNotificationManager;
    private String trackMusic = "Blink 102 FM";
    private String TAG_NOTIFICATION = "NOTIFICATION";
    private Notification.Builder mBuilder;
    private NotificationCompat.Builder mBuilderOreo;
    private Intent mIntent;
    private PendingIntent mPendingIntent;
    private Notification.MediaStyle mStyle;
    private int NOTIFICATION_ID = 101;
    private boolean isStart = true;
    private RemoteViews notificationLayout;


    @Override
    public void onCreate() {
        super.onCreate();

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mIntent = new Intent(this, MainActivity.class);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mPendingIntent = PendingIntent.getActivity(this, 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, "Channel Name", NotificationManager.IMPORTANCE_LOW);
            mChannel.setLightColor(getColor(R.color.colorPrimary));
            mChannel.setSound(null, null);
            mNotificationManager.createNotificationChannel(mChannel);

            notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_oreo);

            mBuilderOreo = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_radio_black_24dp)
                    .setContentIntent(mPendingIntent)
                    .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                    .setOnlyAlertOnce(true)
                    .setColorized(true)
                    .setCustomContentView(notificationLayout)
                    .setColor(getColor(R.color.colorPrimary))
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setAutoCancel(true);

            startForeground(NOTIFICATION_ID, mBuilderOreo.build());
        } else {
            mBuilder = new Notification.Builder(this);
            mStyle = new Notification.MediaStyle();
            mStyle.setShowActionsInCompactView(0);
        }


        if (mController == null) {
            initMediaSession();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        finishMedia();
        return super.onUnbind(intent);
    }

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


    private Notification.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(getApplicationContext(), MediaService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new Notification.Action.Builder(icon, title, pendingIntent).build();
    }

    private void buildNotificationOreo(int image, String textNotification, String intentAction) {

        if (image != 0) {
            notificationLayout.setImageViewResource(R.id.notification_image, image);
        }

        if (!intentAction.isEmpty()){
            Intent intent = new Intent(getApplicationContext(), MediaService.class);
            intent.setAction(intentAction);
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
            notificationLayout.setOnClickPendingIntent(R.id.notification_image, pendingIntent);
        }

        notificationLayout.setTextViewText(R.id.notification_title, textNotification);
        mBuilderOreo.setCustomContentView(notificationLayout);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilderOreo.build());
    }

    private void buildNotification(Notification.Action action, String textNotification) {

        mBuilder.setSmallIcon(R.drawable.ic_radio_black_24dp)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(textNotification)
                .setOngoing(true)
                .setContentIntent(mPendingIntent)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .setStyle(mStyle)
                .addAction(action);

        startForeground(NOTIFICATION_ID, mBuilder.build());
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initMediaSession() {

        mSession = new MediaSession(getApplicationContext(), "example player session");
        mController = new MediaController(getApplicationContext(), mSession.getSessionToken());

        mSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                mSession.setActive(true);
                doService(true, null);
            }

            @Override
            public void onPause() {
                super.onPause();
                mSession.setActive(false);
                doService(false, null);
            }
        });
    }

    public class LocalBinder extends Binder {
        public MediaService getServerInstance() {
            return MediaService.this;
        }
    }

    public void playMedia() {

        if (isStart) {
            if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.N_MR1) {
                buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE), getTrackMusic());
            } else {
                buildNotificationOreo(R.drawable.exo_controls_pause, getTrackMusic(), ACTION_PAUSE);
            }

            isStart = false;
        }
    }

    public void pauseMedia() {
        isStart = true;
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.N_MR1) {
            buildNotification(generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY), getTrackMusic());
        } else {
            buildNotificationOreo(R.drawable.exo_controls_play, getTrackMusic(), ACTION_PLAY);
        }
    }

    public void finishMedia() {
        if (mNotificationManager != null) {
            mNotificationManager.cancel(NOTIFICATION_ID);
            mNotificationManager.cancelAll();
            stopForeground(true);
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
        finishMedia();
        super.onDestroy();
    }

    public String getTrackMusic() {
        return trackMusic;
    }

    public void setTrackMusic(String trackMusic) {
        this.trackMusic = trackMusic.isEmpty() ? "Blink 102 Fm" : trackMusic;

        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.N_MR1) {
            mBuilder.setContentText(this.trackMusic);
            mNotificationManager.notify( NOTIFICATION_ID, mBuilder.build());
        } else {
            buildNotificationOreo(0, this.trackMusic, "");
        }
    }
}

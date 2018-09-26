package net.handsmidia.blink102;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends BaseActivity implements ICallClose {

    private Button mBtnPlay;
    private ProgressBar mLoading;
    private SimpleExoPlayer mPlayer;
    private MediaService mService;
    boolean mBounded;
    FrameLayout container;
    public TextView mTitleMusic;
    //private CoverGenerator coverGenerator;
    private boolean timerIndicator = false;
    private Timer timer = new Timer();
    private static boolean isStarted = false;
    private static String trackTitle = "Blink 102 FM";

    private FragmentBlink fragmentBlink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        Utils.setStatusBarColor(this);

        mTitleMusic = (TextView) findViewById(R.id.tv_title_music);
        container = findViewById(R.id.container);

        if(fragmentBlink == null) {
            fragmentBlink = new FragmentBlink();

            FragmentManager fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.container, fragmentBlink);
            fragmentTransaction.commit();
        }

        mBtnPlay = (Button) findViewById(R.id.btnPlay);
        mLoading = (ProgressBar) findViewById(R.id.loading);

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

        TrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);

        TrackSelector trackSelector = new DefaultTrackSelector(trackSelectionFactory);

        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter();
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "mediaPlayerSample"), defaultBandwidthMeter);

        MediaSource mediaSource = new ExtractorMediaSource(Uri.parse(getString(R.string.url_streaming)), dataSourceFactory, extractorsFactory, null, null);

        mPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

        mPlayer.addListener(this);
        mPlayer.prepare(mediaSource);

        mTitleMusic.setText(trackTitle);

        if (Utils.isConnected(this)) {

            Intent intent = new Intent(getApplicationContext(), MediaService.class);
            intent.setAction(MediaService.ACTION_PLAY);
            startService(intent);
        }

        mBtnPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (Utils.isConnected(MainActivity.this)) {
                    if (!mPlayer.getPlayWhenReady()) {
                        mPlayer.setPlayWhenReady(true);
                        mBtnPlay.setBackgroundResource(R.drawable.stop_button);
                        mLoading.setVisibility(View.GONE);

                        if (mService != null) {
                            mService.playMedia();
                            isStarted = true;
                        }
                    } else {
                        mPlayer.setPlayWhenReady(false);
                        mBtnPlay.setBackgroundResource(R.drawable.play_button);

                        if (mService != null) {
                            mService.pauseMedia();
                            isStarted = false;
                        }
                    }
                }
            }
        });


        Button btn1 = (Button)findViewById(R.id.button);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TelaWhats.class));
            }
        });

        Button btn = (Button)findViewById(R.id.button1);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RedeSocial.class));
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        if(!isServiceRunning(mConnection.getClass())) {
            Intent mIntent = new Intent(this, MediaService.class);
            bindService(mIntent, mConnection, BIND_AUTO_CREATE);
            LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("my-event"));
        }
    }

    ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
            mBounded = false;
            mService = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            mBounded = true;
            MediaService.LocalBinder mLocalBinder = (MediaService.LocalBinder) service;
            mService = mLocalBinder.getServerInstance();
        }
    };

    //Aqui é o link das redes sociais Festeja
    public void browser1(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/user/Blink102FM"));
        startActivity(browserIntent);
    }

    public void browser2(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com.br/maps/place/Blink+102+FM/@-20.4568341,-54.5959997,17z/data=!3m1!4b1!4m5!3m4!1s0x9486e8a3a6493eef:0x4f6f3bf5a715671d!8m2!3d-20.4568341!4d-54.593811"));
        startActivity(browserIntent);
    }

    public void browser3(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.blink102.com.br/"));
        startActivity(browserIntent);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String finishApp = intent.getStringExtra("finish");
            boolean valueNotification = intent.getBooleanExtra("message", true);
            Log.d("receiver", "Got message: " + valueNotification);

            if (valueNotification) {
                mPlayer.setPlayWhenReady(true);
                mBtnPlay.setBackgroundResource(R.drawable.stop_button);
                mLoading.setVisibility(View.GONE);


                if (mService != null) {
                    mService.playMedia();
                    isStarted = true;
                    startThread();
                }
            } else {
                mPlayer.setPlayWhenReady(false);
                mBtnPlay.setBackgroundResource(R.drawable.play_button);


                if (mService != null) {
                    mService.pauseMedia();
                    isStarted = false;
                }
            }

            if (finishApp != null) {
                finish();
            }
        }
    };

    public void startThread() {
        if (!timerIndicator) {
            timerIndicator = true;

            timer.schedule(new TimerTask() {
                public void run() {
                    if (isStarted) {
                        URL url;
                        try {
                            url = new URL(getString(R.string.url_streaming));
                            IcyStreamMeta icy = new IcyStreamMeta(url);
                            if (icy.getArtist().length() > 0 && icy.getTitle().length() > 0) {
                                String title = icy.getArtist() + " - " + icy.getTitle();
                                trackTitle = new String(title.getBytes("ISO-8859-1"), "UTF-8");
                            } else {
                                String title = icy.getArtist() + "" + icy.getTitle();
                                trackTitle = new String(title.getBytes("ISO-8859-1"), "UTF-8");
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mTitleMusic.setText(trackTitle);
                                    mService.setTrackMusic(trackTitle);
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, 0, 1000);
        }
    }

    public static String getTrackTitle() {
        return trackTitle;
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        Utils.showMessage(this, this);
    }

    @Override
    public void exit() {
        timer.cancel();
        mPlayer.setPlayWhenReady(false);
        mService.finishMedia();
        mService.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        finish();
    }

    @Override
    public void minimize() {
        moveTaskToBack(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exit();
    }
}

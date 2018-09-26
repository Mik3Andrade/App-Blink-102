package net.handsmidia.blink102.utilities;

import android.app.Application;

import com.androidnetworking.AndroidNetworking;

public class CustomApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AndroidNetworking.initialize(getApplicationContext());
    }
}

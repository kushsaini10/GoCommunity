package com.example.kush.gocommunity;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.widget.Toast;

/**
 * Created by saini on 21-Aug-16.
 */
public class VibrateService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Vibrator v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        // pass the number of millseconds fro which you want to vibrate the phone here we
        // have passed 2000 so phone will vibrate for 2 seconds.

        v.vibrate(200);
        return super.onStartCommand(intent, flags, startId);
    }
}

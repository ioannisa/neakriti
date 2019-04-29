package eu.anifantakis.neakriti.utils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import static eu.anifantakis.neakriti.utils.NeaKritiApp.mPlayerNotificationManager;

/**
 * create a service that runs while the app runs.
 * when the service is killed, it means the app is killed as well.
 * this is useful as to detect app termination so we remove radio notification incase radio is running while terminating.
 *
 * https://stackoverflow.com/questions/21040339/how-to-know-when-my-app-has-been-killed
 */
public class OnClearFromRecentService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("ClearFromRecentService", "Service Started");
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ClearFromRecentService", "Service Destroyed");
    }

    @Override
    /*
      Code to execute when the service is destroyed (aka when the app is destroyed)
     */
    public void onTaskRemoved(Intent rootIntent) {
        Log.e("ClearFromRecentService", "END");
        //Code here
        mPlayerNotificationManager.setPlayer(null);

        stopSelf();
    }
}

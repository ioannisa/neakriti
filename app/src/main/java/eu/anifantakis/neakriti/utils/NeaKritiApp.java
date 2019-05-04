package eu.anifantakis.neakriti.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import eu.anifantakis.neakriti.R;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;

import static eu.anifantakis.neakriti.preferences.SetPrefs.NEAKRITI_NEWS_TEST_TOPIC;
import static eu.anifantakis.neakriti.preferences.SetPrefs.NEAKRITI_NEWS_TOPIC;
import static eu.anifantakis.neakriti.preferences.SetPrefs.NEAKRITI_NEWS_UPDATES_TOPIC;

public class NeaKritiApp extends Application {
    private static GoogleAnalytics sAnalytics;
    private static Tracker sTracker;
    public static SharedPreferences sharedPreferences;
    public static boolean TEST_MODE = false;

    // get static context
    // Source: https://stackoverflow.com/questions/2002288/static-way-to-get-context-in-android
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        MobileAds.initialize(getApplicationContext(), getString(R.string.admob_appid));

        NeaKritiApp.context = getApplicationContext();
        initSharedPrefs();

        sAnalytics = GoogleAnalytics.getInstance(this);
        setupPicasso();
    }

    /**
     * Provides the Application context in static calls
     * @return context
     */
    public static Context getAppContext(){
        return NeaKritiApp.context;
    }

    /**
     * Change the app's language for the given locale
     * source: https://stackoverflow.com/questions/12908289/how-to-change-language-of-app-when-user-selects-language#
     * @param lang the language in which the application is to run. Currently supported "en" and "el".
     */
    public static void setLocale(String lang, Context context) {
        Locale myLocale = new Locale(lang);
        Configuration conf = new Configuration();
        conf.locale = myLocale;
        context.getResources().updateConfiguration(conf, null);
    }

    /**
     * Sets the language based on system preferences
     */
    public static void setLangFromPreferences(Context context){
        // set the system language based on the SharedPreferences (Default is greek).
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getAppContext());
        String lang = prefs.getString(getAppContext().getResources().getString(R.string.pref_app_loc_lang_key), getAppContext().getResources().getString(R.string.loc_greek_id));

        NeaKritiApp.setLocale(lang, context);
    }

    /**
     * This method is part of a compatibility requirement for devices running Android 4.4 or lower
     * https://groups.google.com/forum/#!topic/firebase-talk/tJxjjHtf8Ww
     */
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private void initSharedPrefs(){
        if (sharedPreferences == null)
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (sharedPreferences.getBoolean(getString(R.string.pref_fcm_key), true)) {
            FirebaseMessaging.getInstance().subscribeToTopic(NEAKRITI_NEWS_TOPIC);
        }

        FirebaseMessaging.getInstance().subscribeToTopic(NEAKRITI_NEWS_UPDATES_TOPIC);

        TEST_MODE = sharedPreferences.getBoolean(getString(R.string.pref_test_mode_key), false);

        if (sharedPreferences.getBoolean(getString(R.string.pref_fcm_key), true)) {
            FirebaseMessaging.getInstance().subscribeToTopic(NEAKRITI_NEWS_TEST_TOPIC);
        }

        AppUtils.isNightMode = sharedPreferences.getBoolean(getString(R.string.pref_night_reading_key), false);
    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
        if (sTracker == null) {
            sTracker = sAnalytics.newTracker(R.xml.global_tracker);
        }
        return sTracker;
    }

    /**
     * Setup picasso with 2 days caching via okHttpClient for the received icons
     */
    private void setupPicasso() {
        // Source: https://gist.github.com/iamtodor/eb7f02fc9571cc705774408a474d5dcb
        OkHttpClient okHttpClient1 = new OkHttpClient.Builder()

                .addInterceptor(new Interceptor() {
                    @NonNull
                    @Override
                    public Response intercept(@NonNull Chain chain) throws IOException {
                        Response originalResponse = chain.proceed(chain.request());

                        int days=2;
                        long cacheTime = 60 * 60 * 24 * days;

                        return originalResponse.newBuilder().header("Cache-Control", "max-age=" + (cacheTime))
                                .build();
                    }
                })
                .cache(new Cache(getCacheDir(), Integer.MAX_VALUE))
                .build();

        Picasso picasso = new Picasso
                .Builder(this)
                .downloader(new OkHttp3Downloader(okHttpClient1))
                .build();

        Picasso.setSingletonInstance(picasso);

        File[] files=getCacheDir().listFiles();
        Log.d("FILES IN CACHE", ""+files.length);

        // indicator for checking picasso caching - need to comment out on release
        //picasso.setIndicatorsEnabled(true);
    }
}

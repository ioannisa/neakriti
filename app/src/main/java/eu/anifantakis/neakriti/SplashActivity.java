package eu.anifantakis.neakriti;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import eu.anifantakis.neakriti.utils.AppUtils;
import eu.anifantakis.neakriti.utils.NeaKritiApp;

import static eu.anifantakis.neakriti.utils.NeaKritiApp.TEST_MODE;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        // set the system language based on the SharedPreferences (Default is greek).
        NeaKritiApp.setLangFromPreferences(getBaseContext());

        initFirebaseRemoteConfig();

        // fetch new configuration for next use
        fetchFirebaseRemoteConfigFromCloud();
    }

    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    /**
     * Initializations to the firebase remote configuration and application of the "actual"  configuration to the application
     */
    private void initFirebaseRemoteConfig(){
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
    }

    private String getFireBaseString(String field){
        String value = mFirebaseRemoteConfig.getString(field);
        value = value.replace("&amp;", "&");
        return value;
    }

    /**
     * Applies current configuration from firebase defaults or cloud settings
     */
    private void applyFirebaseConfiguration(){
        AppUtils.URL_BASE = getFireBaseString("URL_BASE_V2");
        AppUtils.RADIO_STATION_URL = getFireBaseString("RADIO_STATION_URL");
        AppUtils.TV_STATION_URL = getFireBaseString("TV_STATION_URL");
        AppUtils.CHROMECAST_TV_DRAWABLE_URL = getFireBaseString("CHROMECAST_TV_DRAWABLE_URL");

        try {AppUtils.MAIN_1_CAT_POS = Integer.parseInt(mFirebaseRemoteConfig.getString("MAIN_1_CAT_POS")); AppUtils.MAIN_1_CAT_NAME = mFirebaseRemoteConfig.getString("MAIN_1_CAT_NAME"); }catch(Exception e){ AppUtils.MAIN_1_CAT_POS = -1; AppUtils.MAIN_1_CAT_NAME = ""; }
        try {AppUtils.MAIN_2_CAT_POS = Integer.parseInt(mFirebaseRemoteConfig.getString("MAIN_2_CAT_POS")); AppUtils.MAIN_2_CAT_NAME = mFirebaseRemoteConfig.getString("MAIN_2_CAT_NAME"); }catch(Exception e){ AppUtils.MAIN_2_CAT_POS = -1; AppUtils.MAIN_2_CAT_NAME = ""; }
        try {AppUtils.MAIN_3_CAT_POS = Integer.parseInt(mFirebaseRemoteConfig.getString("MAIN_3_CAT_POS")); AppUtils.MAIN_3_CAT_NAME = mFirebaseRemoteConfig.getString("MAIN_3_CAT_NAME"); }catch(Exception e){ AppUtils.MAIN_3_CAT_POS = -1; AppUtils.MAIN_3_CAT_NAME = ""; }

        if (TEST_MODE) Toast.makeText(getApplicationContext(), "TEST_1: "+getFireBaseString("URL_BASE_V2"), Toast.LENGTH_LONG).show();
    }

    /**
     * When called it fetches latest remote configuration from the Firebase Cloud service.
     */
    private void fetchFirebaseRemoteConfigFromCloud(){
        long cacheExpiration = 3600L;
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled())
            cacheExpiration = 0L;

        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mFirebaseRemoteConfig.activateFetched();
                        }
                        applyFirebaseConfiguration();

                        // Start home activity
                        startActivity(new Intent(SplashActivity.this, ArticleListActivity.class));
                        finish();
                    }
                });
    }
}

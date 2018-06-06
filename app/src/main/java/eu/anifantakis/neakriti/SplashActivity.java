package eu.anifantakis.neakriti;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import eu.anifantakis.neakriti.utils.AppUtils;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        // Start home activity
        startActivity(new Intent(SplashActivity.this, ArticleListActivity.class));
        // close splash activity

        initFirebaseRemoteConfig();
        // apply default or last fetched configuration
        applyFirebaseConfiguration();

        // fetch new configuration for next use
        fetchFirebaseRemoteConfigFromCloud();
    }



    public FirebaseRemoteConfig mFirebaseRemoteConfig;

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
                        if (task.isSuccessful()){
                            mFirebaseRemoteConfig.activateFetched();
                            //Toast.makeText(MainActivity.this, "fetched new settings successfully", Toast.LENGTH_LONG).show();
                            applyFirebaseConfiguration();
                        }
                        else{
                            applyFirebaseConfiguration();
                        }
                        finish();
                    }
                });
    }
}

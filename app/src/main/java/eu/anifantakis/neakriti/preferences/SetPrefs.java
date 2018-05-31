package eu.anifantakis.neakriti.preferences;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.firebase.messaging.FirebaseMessaging;

import eu.anifantakis.neakriti.R;

public class SetPrefs extends AppCompatPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String NEAKRITI_NEWS_TOPIC = "neakriti-android-news-test";
    private static PackageInfo pInfo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.action_settings));
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();
        onSharedPreferenceChanged(null, "");

        try {
            // displaying the version number as it is declared in the app build.gradle
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        }catch(Exception e){}

        /*
        LinearLayout root = (LinearLayout)findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.activity_set_prefs, root, false);
        root.addView(bar, 0); // insert at top
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });*/
    }

    public static class PrefsFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            // the "about" (informative only) section of the preferences
            findPreference(getString(R.string.pref_about_version_key)).setSummary(pInfo.versionName);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_fcm_key))) {
            if (sharedPreferences.getBoolean(key, true)) {
                FirebaseMessaging.getInstance().subscribeToTopic(NEAKRITI_NEWS_TOPIC);
            } else {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(NEAKRITI_NEWS_TOPIC);
            }
        }
    }
}

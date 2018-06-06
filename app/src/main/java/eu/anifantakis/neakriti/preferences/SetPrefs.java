package eu.anifantakis.neakriti.preferences;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.firebase.messaging.FirebaseMessaging;

import eu.anifantakis.neakriti.ArticleListActivity;
import eu.anifantakis.neakriti.R;
import eu.anifantakis.neakriti.utils.AppUtils;
import eu.anifantakis.neakriti.widget.NewsWidgetProvider;

import static eu.anifantakis.neakriti.utils.AppUtils.isNightMode;

public class SetPrefs extends AppCompatPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String NEAKRITI_NEWS_TOPIC = "neakriti-android-news-test";

    private static PackageInfo pInfo;
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArticleListActivity.shouldreload = false;

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.action_settings));
        // Show the Up button in the action bar.
        actionBar.setDisplayHomeAsUpEnabled(true);

        // manage the widget setup here as well ;)
        assignAppWidgetId();

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();
        onSharedPreferenceChanged(null, "");

        // displaying the version number as it is declared in the app build.gradle
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Useful method to get activity intent if this activity was a result of a PendingIntent
     * @param newIntent the incoming intent
     */
    @Override
    public void onNewIntent(Intent newIntent) {
        this.setIntent(newIntent);
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
        else if (key.equals(getString(R.string.prefs_widget_category_id))) {
            // set the category name as a shared preference according to the category id so that the widget displays that name as category name

            String[] categoryIds = getResources().getStringArray(R.array.widget_category_ids);
            String[] categoryNames = getResources().getStringArray(R.array.widget_category_names);
            int index = getIndexOfStringArrayWhereStringIs(categoryIds, sharedPreferences.getString(key, getString(R.string.nav_home_id)));
            String selectedCategoryName = categoryNames[index];

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(getString(R.string.prefs_widget_category_title), selectedCategoryName);
            editor.apply();

            // force widget refresh if this is a widget update (not a new widget)
            refreshWidget();
        }
        else if (key.equals(getString(R.string.prefs_widget_category_items))){
            refreshWidget();
        }
        else if (key.equals(getString(R.string.pref_night_reading_key))){
            AppUtils.isNightMode = sharedPreferences.getBoolean(getString(R.string.pref_night_reading_key), false);

            // the "recreate()" method forces new theme to be applied in case we have switched from day to night theme (or vice versa)
            recreate();
        }
    }

    private int getIndexOfStringArrayWhereStringIs(String[] array, String string){
        int index = -1;

        for (int i=0; i<array.length; i++) {
            if (array[i].equals(string)) {
                index = i;
                break;
            }
        }

        return index;
    }

    private void refreshWidget(){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetId = appWidgetManager.getAppWidgetIds(new ComponentName(this, NewsWidgetProvider.class));
        NewsWidgetProvider.onUpdateMyView(this,appWidgetId);
    }

    /**
     * Widget configuration activity,always receives appwidget Id appWidget Id =
     * unique id that identifies your widget analogy : same as setting view id
     * via @+id/viewname on layout but appwidget id is assigned by the system
     * itself
     */
    private void assignAppWidgetId() {
        Bundle extras = getIntent().getExtras();
        if (extras != null)
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    /**
     * This method is required for Lollipop and Bellow (API 22 and lower) for the back button of the
     * Preferences Activity to be functional
     * Source: https://stackoverflow.com/questions/37222879/add-action-bar-with-back-button-in-preference-activity
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Resources.Theme getTheme() {
        Resources.Theme theme = super.getTheme();
        if(isNightMode){
            theme.applyStyle(R.style.NightDarkActionBar, true);
        }
        else{
            theme.applyStyle(R.style.DarkActionBar, true);
        }
        return super.getTheme();
    }
}

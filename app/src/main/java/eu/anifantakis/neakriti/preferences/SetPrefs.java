package eu.anifantakis.neakriti.preferences;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import androidx.core.app.NavUtils;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;

import com.google.firebase.messaging.FirebaseMessaging;

import eu.anifantakis.neakriti.ArticleListActivity;
import eu.anifantakis.neakriti.R;
import eu.anifantakis.neakriti.utils.AppUtils;
import eu.anifantakis.neakriti.widget.NewsWidgetProvider;

import static eu.anifantakis.neakriti.utils.AppUtils.isNightMode;
import static eu.anifantakis.neakriti.utils.NeaKritiApp.TEST_MODE;

public class SetPrefs extends AppCompatPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String NEAKRITI_NEWS_TOPIC = "neakriti-android-news";
    public static final String NEAKRITI_NEWS_TEST_TOPIC = "neakriti-android-news-test";
    public static final String NEAKRITI_NEWS_UPDATES_TOPIC = "neakriti-android-updates";

    private static PackageInfo pInfo;
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArticleListActivity.RESTART_REQUIRED = false;
        ArticleListActivity.shouldreload = false;

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.action_settings));
        // Show the Up button in the action bar.
        actionBar.setDisplayHomeAsUpEnabled(true);

        // manage the widget setup here as well ;)
        assignAppWidgetId();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
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
            findPreference(getString(R.string.pref_about_version_key)).setSummary(pInfo.versionName + " ("+getString(R.string.build)+" "+pInfo.versionCode+")");

            // if we made the secret key combo to show the option for the test notifications
            // then show them in the screen
            if (TEST_MODE) {
                PreferenceCategory targetCategory = (PreferenceCategory) findPreference(getString(R.string.pref_fcm_category_key));
                CheckBoxPreference checkBoxPreference = new CheckBoxPreference(getActivity());
                checkBoxPreference.setKey(getString(R.string.pref_fcm_test_key));
                checkBoxPreference.setTitle(getString(R.string.pref_fcm_test_title));
                checkBoxPreference.setSummary(getString(R.string.pref_fcm_test_summary));
                targetCategory.addPreference(checkBoxPreference);
            }

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
        else if (key.equals(getString(R.string.pref_fcm_test_key))) {
            if (sharedPreferences.getBoolean(key, true)) {
                FirebaseMessaging.getInstance().subscribeToTopic(NEAKRITI_NEWS_TEST_TOPIC);
            } else {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(NEAKRITI_NEWS_TEST_TOPIC);
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
        else if (key.equals(getString(R.string.pref_app_loc_lang_key))){


            Log.d("PREFS", sharedPreferences.getString(key, "XXX"));
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(key, sharedPreferences.getString(key, "XXX"));
            editor.apply();


            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setTitle(R.string.dlg_restart_title)
                    .setMessage(R.string.dlg_restart_body)
                    .setIcon(R.drawable.warning_48px)
                    .setNegativeButton(R.string.dlg_restart_close, (dialog, id) -> {
                        ArticleListActivity.RESTART_REQUIRED = true;
                        finish();
                    }
                    );
            // Create the AlertDialog object and return it
            builder.create().show();
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
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
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

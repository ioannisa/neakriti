package eu.anifantakis.neakriti.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import eu.anifantakis.neakriti.R;

import static eu.anifantakis.neakriti.utils.AppUtils.PREFS_WIDGET_CATEGORY_ID;
import static eu.anifantakis.neakriti.utils.AppUtils.PREFS_WIDGET_CATEGORY_ORDER;
import static eu.anifantakis.neakriti.utils.AppUtils.PREFS_WIDGET_CATEGORY_TITLE;

public class WidgetConfigActivity extends AppCompatActivity implements View.OnClickListener {
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    Category[] categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_config);

        categories = new Category[]{
                new Category(getString(R.string.nav_home_id), getString(R.string.nav_home), R.drawable.home),
                new Category(getString(R.string.nav_crete_id), getString(R.string.nav_crete), R.drawable.map_marker),
                new Category(getString(R.string.nav_views_id), getString(R.string.nav_views), R.drawable.message_text),
                new Category(getString(R.string.nav_economy_id), getString(R.string.nav_economy), R.drawable.currency_eur),
                new Category(getString(R.string.nav_culture_id), getString(R.string.nav_culture), R.drawable.school),
                new Category(getString(R.string.nav_pioneering_id), getString(R.string.nav_pioneering), R.drawable.satellite_variant),
                new Category(getString(R.string.nav_sports), getString(R.string.nav_sports), R.drawable.soccer),
                new Category(getString(R.string.nav_lifestyle_id), getString(R.string.nav_lifestyle), R.drawable.tie),
                new Category(getString(R.string.nav_health_id), getString(R.string.nav_health), R.drawable.hospital),
                new Category(getString(R.string.nav_woman_id), getString(R.string.nav_woman), R.drawable.gender_female),
                new Category(getString(R.string.nav_travel_id), getString(R.string.nav_travel), R.drawable.wallet_travel)
        };

        assignAppWidgetId();
        findViewById(R.id.widgetStartButton).setOnClickListener(this);
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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.widgetStartButton)
            updateWidget(0);
    }

    class Category{
        String id, title;
        int drawable;

        Category(String id, String title, int drawable){
            this.id = id;
            this.title = title;
            this.drawable = drawable;
        }
    }



    private void updateWidget(int categoryOrder){


        //Upon click on categories we write inside sharedprefs the id of the category so to use it inside
        //WidgetFetchArticleService and fetch articles of the specific category
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(PREFS_WIDGET_CATEGORY_ORDER, categoryOrder);
        editor.putString(PREFS_WIDGET_CATEGORY_ID, categories[categoryOrder].id);
        editor.putString(PREFS_WIDGET_CATEGORY_TITLE, categories[categoryOrder].title);
        editor.apply();

        // this intent is essential to show the widget
        // if this intent is not included,you can't show
        // widget on homescreen
        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(Activity.RESULT_OK, intent);

        //We call onUpdateMyView of the widgetProvider passing the widgetId[]
        //upon click of a category update widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetId = appWidgetManager.getAppWidgetIds(new ComponentName(this, NewsWidgetProvider.class));

        NewsWidgetProvider.onUpdateMyView(this,appWidgetId);

        finish();
    }

    /**
     * This method right now displays the widget and starts a Service to fetch
     * remote data from Server
     */
    private void startWidget() {

        // this intent is essential to show the widget
        // if this intent is not included,you can't show
        // widget on homescreen
        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(Activity.RESULT_OK, intent);

        // start your service
        // to fetch data from web
        Intent serviceIntent = new Intent(this, WidgetFetchArticlesService.class);
        serviceIntent
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        startService(serviceIntent);

        // finish this activity
        this.finish();

    }
}

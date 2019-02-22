package eu.anifantakis.neakriti.widget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViewsService;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * https://laaptu.wordpress.com/2013/07/19/android-app-widget-with-listview/
 */
public class WidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.d("WIDGET_REMOTE_SERVICE", "CALLED");

        int appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        // if the widget's array list is garbage collected, get the list data from the preferences
        if (WidgetFetchArticlesService.listItemList==null) {
            Log.d("WIDGET_REMOTE_SERVICE", "WIDGET LIST IS NULL - RECOVERING...");
            loadArrayListFromPreferences();
        }

        return (new ListProvider(this.getApplicationContext(), intent));
    }

    /**
     * if the widget's array list is garbage collected, get the list data from the preferences
     */
    private void loadArrayListFromPreferences(){
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        String json = appSharedPrefs.getString("MyObject", "");

        // About converting from Jon to a typed ArrayList<T>
        // https://stackoverflow.com/questions/12384064/gson-convert-from-json-to-a-typed-arraylistt
        WidgetFetchArticlesService.listItemList = gson.fromJson(json, new TypeToken<ArrayList<ListProvider.ListItem>>(){}.getType());
    }
}

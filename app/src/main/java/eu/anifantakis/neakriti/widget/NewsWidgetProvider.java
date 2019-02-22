package eu.anifantakis.neakriti.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RemoteViews;

import eu.anifantakis.neakriti.ArticleDetailActivity;
import eu.anifantakis.neakriti.R;
import eu.anifantakis.neakriti.preferences.SetPrefs;
import eu.anifantakis.neakriti.utils.AppUtils;

public class NewsWidgetProvider extends AppWidgetProvider {

    public static final String APPWIDGET_UPDATE="android.appwidget.action.APPWIDGET_UPDATE";

    public static final String WIDGET_EXTRAS_HAS_DATA = "eu.anifantakis.neakriti.HAS_DATA";
    public static final String WIDGET_EXTRAS_CATGORY_TITLE = "eu.anifantakis.neakriti.CATEGORY_TITLE";
    public static final String WIDGET_EXTRAS_DIRECTION = "eu.anifantakis.neakriti.DIRECTION";

    public static final int WIDGET_DIRECTION_EXISTING = 0;
    public static final int WIDGET_DIRECTION_PREVIOUS = 1;
    public static final int WIDGET_DIRECTION_NEXT     = 2;

    public static long lastNoHasData = 0;

    private RemoteViews updateAppWidget(Context context, int appWidgetId, boolean hasData, CharSequence categoryTitle) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.news_widget_provider);

        //RemoteViews Service needed to provide adapter for ListView
        Intent svcIntent = new Intent(context, WidgetService.class);

        //passing app widget id to that RemoteViews Service
        svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        //setting a unique Uri to the intent
        svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

        if (hasData) {
            Log.d("WIDGET_HAS_DATA", "TRUE");
            //setting adapter to listview of the widget
            views.setRemoteAdapter(R.id.list_view_widget,
                    svcIntent);

            // ListView Item Click launches the ArticleDetailActivity to display that item
            Intent intent = new Intent(context, ArticleDetailActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.list_view_widget, pendingIntent);
        }

        // set the category title
        views.setTextViewText(R.id.widget_title, categoryTitle);

        // Reload Button
        Intent serviceIntent = new Intent(context, WidgetFetchArticlesService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getService(context, WIDGET_DIRECTION_EXISTING, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_reload, pendingIntent);

        // Previous Button
        Intent prvIntent = new Intent(context, WidgetFetchArticlesService.class);
        prvIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        prvIntent.putExtra(WIDGET_EXTRAS_DIRECTION, WIDGET_DIRECTION_PREVIOUS);
        PendingIntent prvPendingIntent = PendingIntent.getService(context, WIDGET_DIRECTION_PREVIOUS, prvIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_category_prv, prvPendingIntent);

        // Next Button
        Intent nextIntent = new Intent(context, WidgetFetchArticlesService.class);
        nextIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        nextIntent.putExtra(WIDGET_EXTRAS_DIRECTION, WIDGET_DIRECTION_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getService(context, WIDGET_DIRECTION_NEXT, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_category_next, nextPendingIntent);

        // Settings Button
        Intent settingsIntent = new Intent(context, SetPrefs.class);
        settingsIntent.putExtra(APPWIDGET_UPDATE, true);
        PendingIntent settingsPendingIntent = PendingIntent.getActivity(context, 10, settingsIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_settings, settingsPendingIntent);

        // if the list_view_widget is empty, then show the text view that contains the empty text
        views.setEmptyView(R.id.list_view_widget, R.id.list_view_empty_text);

        return views;
    }

    private static int[] savedAppWidgetIds;

    public static void onUpdateMyView(Context context, int[] widgetIds) {
        // this method is called upon generation of the widget and each time we click on nav drawer buttons
        savedAppWidgetIds = widgetIds;
        if (savedAppWidgetIds != null){
            for (int savedAppWidgetId : savedAppWidgetIds) {
                //we start the service
                Intent serviceIntent = new Intent(context, WidgetFetchArticlesService.class);
                serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, savedAppWidgetId);
                //context.startService(serviceIntent);

                //Fixing java.lang.IllegalStateException: Not allowed to start service Intent
                //Source: https://stackoverflow.com/questions/46445265/android-8-0-java-lang-illegalstateexception-not-allowed-to-start-service-inten
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent);
                } else {
                    context.startService(serviceIntent);
                }
            }
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        savedAppWidgetIds = appWidgetIds;
        onUpdateMyView(context, appWidgetIds);

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    /**
     * It receives the broadcast as per the action set on intent filters on
     * Manifest.xml once data is fetched from RemotePostService,it sends
     * broadcast and WidgetProvider notifies to change the data the data change
     * right now happens on ListProvider as it takes RemoteFetchService
     * listItemList as data
     */
    //broadcast is received
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);



        Log.d("WIDGET_RECEIVER", "ON RECEIVE");

        if (intent.getAction().equals(APPWIDGET_UPDATE)) {
            int appWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

            boolean hasData = intent.getBooleanExtra(
                    WIDGET_EXTRAS_HAS_DATA,
                    true
            );



            String categoryTitle = intent.getStringExtra(WIDGET_EXTRAS_CATGORY_TITLE);

            AppWidgetManager appWidgetManager = AppWidgetManager
                    .getInstance(context);

            Log.d("WIDGET RECEIVER", "DATA FETCHED");
            //RemoteViews remoteViews = updateAppWidget(context, appWidgetId, hasData);
            //appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

            //call updateAppWidget method passing the attributes
            RemoteViews remoteViews = updateAppWidget(context, appWidgetId, hasData, categoryTitle);
            //calling this method inside listprovider to trigger populateListItem method ()
            ListProvider.setInfoData();
            //NESECCARY line of code..watch out the attributes
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.list_view_widget);
            //call update at manager
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }
}

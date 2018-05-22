package eu.anifantakis.neakriti.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import eu.anifantakis.neakriti.ArticleListActivity;
import eu.anifantakis.neakriti.R;

public class NewsWidgetProvider extends AppWidgetProvider {

    public static final String APPWIDGET_UPDATE="android.appwidget.action.APPWIDGET_UPDATE";
    public static final String APPWIDGET_NOITEMS="eu.anifantakis.neakriti.APPWIDGET_NOITEMS";

    private RemoteViews updateAppWidget(Context context, int appWidgetId, boolean hasData) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.news_widget_provider);

        //RemoteViews Service needed to provide adapter for ListView
        Intent svcIntent = new Intent(context, WidgetService.class);

        //passing app widget id to that RemoteViews Service
        svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        //setting a unique Uri to the intent
        svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

        if (hasData) {
            //setting adapter to listview of the widget
            views.setRemoteAdapter(appWidgetId, R.id.list_view_widget,
                    svcIntent);
        }

        // if the list_view_widget is empty, then show the text view that contains the empty text
        views.setEmptyView(R.id.list_view_widget, R.id.list_view_empty_text);

        //updateAppWidget(context,appWidgetId);
        return views;
        /*

        views.setTextViewText(R.id.tv_widget_title, "TITLE");

        // open the app if someone click on either of the two textviews of the widget
        Intent intent = new Intent(context, ArticleListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.tv_widget_title, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);*/

    }

    private static int[] savedAppWidgetIds;

    public static void onUpdateMyView(Context context) {
        final int N = savedAppWidgetIds.length;
        for (int i = 0; i < N; i++) {
            Intent serviceIntent = new Intent(context, WidgetFetchArticlesService.class);
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, savedAppWidgetIds[i]);

            try {
                context.startService(serviceIntent);
            }
            catch(IllegalStateException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        savedAppWidgetIds = appWidgetIds;
        onUpdateMyView(context);

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    /**
     * It receives the broadcast as per the action set on intent filters on
     * Manifest.xml once data is fetched from RemotePostService,it sends
     * broadcast and WidgetProvider notifies to change the data the data change
     * right now happens on ListProvider as it takes RemoteFetchService
     * listItemList as data
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d("WIDGET RECEIVER", "ON RECEIVE");

        if (intent.getAction().equals(APPWIDGET_UPDATE)) {
            int appWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

            boolean hasData = intent.getBooleanExtra(
                    "HAS_DATA",
                    true
            );

            if (hasData){
                Log.d("WIDGET RECEIVER", "HAS DATA: TRUE");
            }
            else{
                Log.d("WIDGET RECEIVER", "HAS DATA: FALSE");
            }

            AppWidgetManager appWidgetManager = AppWidgetManager
                    .getInstance(context);

            Log.d("WIDGET RECEIVER", "DATA FETCHED");
            RemoteViews remoteViews = updateAppWidget(context, appWidgetId, hasData);
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }
}

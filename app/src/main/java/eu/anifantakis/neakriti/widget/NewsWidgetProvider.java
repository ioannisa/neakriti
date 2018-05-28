package eu.anifantakis.neakriti.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import eu.anifantakis.neakriti.ArticleDetailActivity;
import eu.anifantakis.neakriti.R;

public class NewsWidgetProvider extends AppWidgetProvider {

    public static final String APPWIDGET_UPDATE="android.appwidget.action.APPWIDGET_UPDATE";
    public static final String APPWIDGET_NOITEMS="eu.anifantakis.neakriti.APPWIDGET_NOITEMS";

    public static final String WIDGET_EXTRAS_HAS_DATA = "eu.anifantakis.neakriti.HAS_DATA";
    public static final String WIDGET_EXTRAS_CATGORY_TITLE = "eu.anifantakis.neakriti.CATEGORY_TITLE";

    private RemoteViews updateAppWidget(Context context, int appWidgetId, boolean hasData, CharSequence categoryTitle) {
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
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_reload, pendingIntent);

        // if the list_view_widget is empty, then show the text view that contains the empty text
        views.setEmptyView(R.id.list_view_widget, R.id.list_view_empty_text);

        return views;
    }

    private static int[] savedAppWidgetIds;

    public static void updateView(Context context){
        final int N = savedAppWidgetIds.length;
        for (int i = 0; i < N; i++) {
            Intent serviceIntent = new Intent(context, WidgetFetchArticlesService.class);
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, savedAppWidgetIds[i]);
            context.startService(serviceIntent);
        }
    }

    public static void onUpdateMyView(Context context, int[] widgetIds) {
        // this method is called upon generation of the widget and each time we click on nav drawer buttons
        savedAppWidgetIds = widgetIds;
        if (savedAppWidgetIds != null){
            final int N = savedAppWidgetIds.length;
            for (int i = 0; i < N; i++) {

                //we start the service
                Intent serviceIntent = new Intent(context, WidgetFetchArticlesService.class);
                serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, savedAppWidgetIds[i]);
                context.startService(serviceIntent);
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

        Log.d("WIDGET RECEIVER", "ON RECEIVE");

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

package eu.anifantakis.neakriti.widget;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import eu.anifantakis.neakriti.R;
import eu.anifantakis.neakriti.data.feed.RequestInterface;
import eu.anifantakis.neakriti.data.feed.gson.Article;
import eu.anifantakis.neakriti.data.feed.gson.Feed;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static eu.anifantakis.neakriti.utils.AppUtils.URL_BASE;
import static eu.anifantakis.neakriti.utils.AppUtils.spec;
import static eu.anifantakis.neakriti.widget.NewsWidgetProvider.APPWIDGET_UPDATE;
import static eu.anifantakis.neakriti.widget.NewsWidgetProvider.WIDGET_DIRECTION_NEXT;
import static eu.anifantakis.neakriti.widget.NewsWidgetProvider.WIDGET_DIRECTION_PREVIOUS;
import static eu.anifantakis.neakriti.widget.NewsWidgetProvider.WIDGET_EXTRAS_CATGORY_TITLE;
import static eu.anifantakis.neakriti.widget.NewsWidgetProvider.WIDGET_EXTRAS_DIRECTION;
import static eu.anifantakis.neakriti.widget.NewsWidgetProvider.WIDGET_EXTRAS_HAS_DATA;

// source: https://laaptu.wordpress.com/2013/07/24/populate-appwidget-listview-with-remote-datadata-from-web/

public class WidgetFetchArticlesService extends Service {
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    public static Feed widgetFeed = null;
    public static ArrayList<ListProvider.ListItem> listItemList;
    public static String categoryTitle = "";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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

    /**
     * Fixing java.lang.IllegalStateException: Not allowed to start service Intent
     * Source: https://stackoverflow.com/questions/46445265/android-8-0-java-lang-illegalstateexception-not-allowed-to-start-service-inten
     */
    @Override
    public void onCreate() {
        super.onCreate();


        // Due to calling startForegroundService in NewsWidgetProvider.java we have follow it with a "startForeground" method.
        // This method needs to have a mandatory Notification call.  Therefore we include this unnecessary Notification.

        // This notification is useless and does not effect the app if its on or off.  We just need it to call the "startForeground" method.
        // This is since we cannot pass "null" to the notification whatsoever.

        // Hence we are using an already existing channel (Which we don't care if its active or inactive) - we will lend the news notification channel here

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = getString(R.string.notif_channel_news_id);
        String channelName = getString(R.string.notif_channel_news_name);
        int importance = 0;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            importance = NotificationManager.IMPORTANCE_NONE;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }
        startForeground(1, new NotificationCompat.Builder( this, channelId).build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("WIDGET_FETCH SERVICE", "ON START");
        if (intent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID))
            Log.d("WIDGET_FETCH SERVICE", "STARTED");
            appWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

        Log.d("WIDGET_FETCH", "SERVICE ON START");
        if (intent.hasExtra(WIDGET_EXTRAS_DIRECTION)){
            Log.d("WIDGET_DIRECTION", "EXISTS");
            int direction = intent.getIntExtra(WIDGET_EXTRAS_DIRECTION, 0);
            if (direction>0){
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                int order = sharedPreferences.getInt(getString(R.string.prefs_widget_category_order), 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                Category[] categories = {
                        new Category(getString(R.string.nav_home_id),       getString(R.string.nav_home),       R.drawable.home),
                        new Category(getString(R.string.nav_crete_id),      getString(R.string.nav_crete),      R.drawable.map_marker),
                        new Category(getString(R.string.nav_views_id),      getString(R.string.nav_views),      R.drawable.message_text),
                        new Category(getString(R.string.nav_economy_id),    getString(R.string.nav_economy),    R.drawable.currency_eur),
                        new Category(getString(R.string.nav_culture_id),    getString(R.string.nav_culture),    R.drawable.school),
                        new Category(getString(R.string.nav_pioneering_id), getString(R.string.nav_pioneering), R.drawable.satellite_variant),
                        new Category(getString(R.string.nav_sports_id),        getString(R.string.nav_sports),     R.drawable.soccer),
                        new Category(getString(R.string.nav_lifestyle_id),  getString(R.string.nav_lifestyle),  R.drawable.tie),
                        new Category(getString(R.string.nav_health_id),     getString(R.string.nav_health),     R.drawable.hospital),
                        new Category(getString(R.string.nav_woman_id),      getString(R.string.nav_woman),      R.drawable.gender_female),
                        new Category(getString(R.string.nav_travel_id),     getString(R.string.nav_travel),     R.drawable.wallet_travel)
                };

                if (direction==WIDGET_DIRECTION_PREVIOUS){
                    Log.d("WIDGET DIRECTION", "PREVIOUS");
                    if (order>0){
                        order--;
                    }
                    else{
                        order = categories.length-1;
                    }
                }
                else if (direction==WIDGET_DIRECTION_NEXT){
                    Log.d("WIDGET DIRECTION", "NEXT");
                    if (order == categories.length-1){
                        order = 0;
                    }
                    else{
                        order++;
                    }
                }
                editor.putInt(getString(R.string.prefs_widget_category_order), order);
                editor.putString(getString(R.string.prefs_widget_category_id), categories[order].id);
                editor.putString(getString(R.string.prefs_widget_category_title), categories[order].title);
                editor.apply();
            }
        }

        fetchDataFromWeb();
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * We are not in an IntentService.  So make async retrofit call, not to block the main thread
     */
    private void fetchDataFromWeb() {
        Log.d("WIDGET_FETCH_SERVICE", "FETCH DATA FROM: " + URL_BASE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_BASE)
                .client(new OkHttpClient.Builder().connectionSpecs(Collections.singletonList(spec)).build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        //everytime the widget service fetches data first we get the number of the articles
        //category to get the info
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String categoryId = sharedPreferences.getString(getString(R.string.prefs_widget_category_id), getString(R.string.nav_home_id));
        categoryTitle = sharedPreferences.getString(getString(R.string.prefs_widget_category_title), getString(R.string.nav_home));
        final int items = Integer.parseInt(Objects.requireNonNull(sharedPreferences.getString(getString(R.string.prefs_widget_category_items), "5")));

        RequestInterface request = retrofit.create(RequestInterface.class);
        Call<Feed> call = request.getFeedByCategory(categoryId, items);
        call.enqueue(new Callback<Feed>() {
            @Override
            public void onResponse(@NonNull Call<Feed> call, @NonNull Response<Feed> response) {
                widgetFeed = response.body();

                // widgetFeed will be null if server is not responding - SERVER DOWN
                if (widgetFeed==null){
                    populateWidget(false);
                }
                else {
                    ArrayList<Article> articleList = widgetFeed.getChannel().getItems();
                    listItemList = new ArrayList<>();
                    int count = 0;
                    for (Article article : articleList) {
                        if (count == items)
                            break;

                        count++;
                        ListProvider.ListItem listItem = new ListProvider.ListItem();
                        listItem.categoryTitle = categoryTitle;
                        listItem.guid = article.getGuid();
                        listItem.title = article.getTitle();
                        listItem.link = article.getLink();
                        listItem.imgThumb = article.getImgThumbStr();
                        listItem.imgLarge = article.getImgLargeStr();
                        listItem.description = article.getDescription();
                        listItem.pubDate = article.getPubDateStr();
                        listItem.pubDateGre = article.getPubDateGre();
                        listItem.updated = article.getUpdatedStr();

                        listItemList.add(listItem);
                    }

                    saveArrayListInPreferences();

                    populateWidget(true);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Feed> call, @NonNull Throwable t) {
                widgetFeed = null;
                populateWidget(false);
                Log.d("WIDGET_FETCH_SERVICE", "FAILED TO GET DATA");
            }

            /**
             * ArrayList<ListProvider.ListItem> listItemList can be garbage collected at any time in our widget
             *
             * Safe keep the array list with the widget articles, since the list object may be garbage collected at any time
             * So we save this easily in json format in our preferences for widget to be used when ArrayList is garbage collected
             */
            private void saveArrayListInPreferences(){
                SharedPreferences appSharedPrefs = PreferenceManager
                        .getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
                Gson gson = new Gson();
                String json = gson.toJson(listItemList);
                prefsEditor.putString("MyObject", json);
                prefsEditor.apply();
            }
        });
    }

    //then we send broadcast
    private void populateWidget(boolean hasData) {
        Log.d("WIDGET_FETCH_SERVICE", "POPULATING WIDGET");
        Intent widgetUpdateIntent = new Intent();
        widgetUpdateIntent.setAction(APPWIDGET_UPDATE);

        widgetUpdateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                appWidgetId);

        widgetUpdateIntent.putExtra(WIDGET_EXTRAS_HAS_DATA, hasData);
        widgetUpdateIntent.putExtra(WIDGET_EXTRAS_CATGORY_TITLE, categoryTitle);

        sendBroadcast(widgetUpdateIntent);

        this.stopSelf();
    }
}

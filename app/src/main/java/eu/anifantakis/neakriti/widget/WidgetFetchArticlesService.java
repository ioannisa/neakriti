package eu.anifantakis.neakriti.widget;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import eu.anifantakis.neakriti.data.RequestInterface;
import eu.anifantakis.neakriti.data.feed.gson.Article;
import eu.anifantakis.neakriti.data.feed.gson.Feed;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static eu.anifantakis.neakriti.utils.AppUtils.URL_BASE;
import static eu.anifantakis.neakriti.widget.NewsWidgetProvider.APPWIDGET_UPDATE;

// source: https://laaptu.wordpress.com/2013/07/24/populate-appwidget-listview-with-remote-datadata-from-web/

public class WidgetFetchArticlesService extends Service {
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    public static Feed widgetFeed = null;
    public static ArrayList<ListProvider.ListItem> listItemList;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("WIDGET FETCH SERVICE", "ON START");
        if (intent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID))
            Log.d("WIDGET FETCH SERVICE", "STARTED");
            appWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

        fetchDataFromWeb();
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * We are not in an IntentService.  So make async retrofit call, not to block the main thread
     */
    private void fetchDataFromWeb() {
        Log.d("WIDGET FETCH SERVICE", "FETCH DATA FROM WEB");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_BASE)
                .client(new OkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestInterface request = retrofit.create(RequestInterface.class);
        Call<Feed> call = request.getFeedByCategory("127", 5);
        call.enqueue(new Callback<Feed>() {
            @Override
            public void onResponse(Call<Feed> call, Response<Feed> response) {
                widgetFeed = response.body();

                ArrayList<Article> articleList = widgetFeed.getChannel().getItems();
                listItemList = new ArrayList<ListProvider.ListItem>();
                int count = 0;
                for (Article article : articleList){
                    if (count==5)
                        break;

                    count++;
                    ListProvider.ListItem listItem = new ListProvider.ListItem();
                    listItem.heading = article.getTitle();
                    listItem.imageUrl = article.getImgThumbStr();
                    listItemList.add(listItem);
                }

                populateWidget(true);
            }

            @Override
            public void onFailure(Call<Feed> call, Throwable t) {
                widgetFeed = null;
                populateWidget(false);
            }
        });
    }

    private void populateWidget(boolean hasData) {
        Log.d("WIDGET FETCH SERVICE", "POPULATING WIDGET");
        Intent widgetUpdateIntent = new Intent();
        widgetUpdateIntent.setAction(APPWIDGET_UPDATE);

        widgetUpdateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                appWidgetId);

        widgetUpdateIntent.putExtra("HAS_DATA", hasData);

        sendBroadcast(widgetUpdateIntent);

        this.stopSelf();
    }
}

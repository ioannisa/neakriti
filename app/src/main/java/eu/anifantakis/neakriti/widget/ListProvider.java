package eu.anifantakis.neakriti.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import java.util.ArrayList;

import eu.anifantakis.neakriti.R;
import eu.anifantakis.neakriti.data.feed.gson.Article;
import eu.anifantakis.neakriti.utils.AppUtils;

// source: https://laaptu.wordpress.com/2013/07/19/android-app-widget-with-listview/
public class ListProvider implements RemoteViewsFactory {
    public static class ListItem {
        String categoryTitle;
        int guid;
        String link;
        public String title;
        String description;
        String pubDate;
        String updated;
        String pubDateGre;
        String imgThumb;
        String imgLarge;
    }

    private static ArrayList<ListItem> listItemList = new ArrayList<>();
    private Context context;

    ListProvider(Context context, Intent intent) {
        this.context = context;
        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        populateListItem();
    }

    private static void populateListItem() {
        /*
        try {
            listItemList = (ArrayList<ListItem>)
                    WidgetFetchArticlesService.listItemList
                            .clone();
        }
        catch(Exception e){
            listItemList = null;
        }*/
        listItemList = WidgetFetchArticlesService.listItemList;
    }

    @Override
    public int getCount() {
        Log.d("WIDGET_VIEWS_FACTORY", "GET COUNT");
        int size = 0;
        try {
            if (listItemList==null) {
                Log.d("WIDGET_VIEWS_FACTORY", "GET COUNT - ListItemList is null");
                populateListItem();
                Log.d("WIDGET_VIEWS_FACTORY", "GET COUNT - ListItemList populated");
            }

            size = listItemList.size();
        }
        catch (NullPointerException e){
            e.printStackTrace();
            Log.d("WIDGET_VIEWS_FACTORY", "GET COUNT - EXCEPTION");
        }
        return size;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /*
     *Similar to getView of Adapter where instead of View
     *we return RemoteViews
     *
     */
    @Override
    public RemoteViews getViewAt(int position) {
        Log.d("WIDGET_VIEWS_FACTORY", "GET ViewAt");
        final RemoteViews remoteView = new RemoteViews(
                context.getPackageName(), R.layout.row_widget_list);

        if (listItemList!=null) {
            ListItem listItem = listItemList.get(position);
            remoteView.setTextViewText(R.id.widget_row_heading, listItem.title);
            remoteView.setImageViewBitmap(R.id.widget_row_image, AppUtils.getBitmapfromUrl(listItem.imgThumb));
            //remoteView.setImageViewBitmap(R.id.widget_row_image, null);

            Article article = new Article();
            article.setGuid(listItem.guid);
            article.setTitle(listItem.title);
            article.setDescription(listItem.description);
            article.setLink(listItem.link);
            article.setImgThumb(listItem.imgThumb);
            article.setImgLarge(listItem.imgLarge);
            article.setPubDateStr(listItem.pubDate);
            article.setPubDateGre(listItem.pubDateGre);
            article.setUpdatedStr(listItem.updated);

            Intent fillInIntent = new Intent();
            fillInIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            fillInIntent.putExtra(AppUtils.EXTRAS_ARTICLE, article);
            fillInIntent.putExtra(AppUtils.EXTRAS_ORIGIN_NOTIFICATION, true);
            fillInIntent.putExtra(AppUtils.EXTRAS_CUSTOM_CATEGORY_TITLE, listItem.categoryTitle);

            remoteView.setOnClickFillInIntent(R.id.widget_row_image, fillInIntent);
            remoteView.setOnClickFillInIntent(R.id.widget_row_heading, fillInIntent);
        }

        return remoteView;
    }


    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
    }

    @Override
    public void onDestroy() {
    }

    //new method to call it from broadcast receiver inside widgetprovider so to trigger populateListItem method
    static void setInfoData() {
        populateListItem();
    }
}
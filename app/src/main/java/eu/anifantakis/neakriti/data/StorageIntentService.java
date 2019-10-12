package eu.anifantakis.neakriti.data;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;

import java.util.Date;

import eu.anifantakis.neakriti.data.db.ArticlesDBContract;
import eu.anifantakis.neakriti.data.feed.ArticlesCollection;
import eu.anifantakis.neakriti.data.feed.gson.Article;
import eu.anifantakis.neakriti.utils.AppUtils;

/**
 * This intent service will store the 20 latest articles per loaded category for offline usage
 * The choice for Intent Service was due to the fact that:
 * this is a "silent job", that can run on a separate thread and does not depend on any UI.
 */
public class StorageIntentService extends IntentService {
    private static final String NEAKRITI_STORAGE_INTENT_SERVICE_NAME = "NeaKritiStorageIntentService";

    public static final String COLLECTION = "collection";

    public StorageIntentService() {
        super(NEAKRITI_STORAGE_INTENT_SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        assert intent != null;
        Bundle extras = intent.getExtras();

        assert extras != null;
        if (extras.containsKey(COLLECTION)) {
            ArticlesCollection collection = extras.getParcelable(COLLECTION);

            // This type of persistence regards categories only.  This if statement acts as a line
            // of defence in case we accidentally initiate this service passing some other type
            // of articles like (Tags, Favorites, Zones, etc).
            assert collection != null;
            if (collection.getListType() == ArticlesDBContract.DB_TYPE_CATEGORY){
                deleteOldCollectionFromCategory(collection.getListId());
                addNewCollectionToCategory(collection);
            }
        }
    }

    /**
     * Delete Old Articles stored for this category
     * @param categoryID The articles with the specified categoryID will be deleted
     */
    private void deleteOldCollectionFromCategory(String categoryID){
        Uri uri = ArticlesDBContract.ArticleEntry.CATGORY_URI;
        uri = uri.buildUpon().appendPath(categoryID).build();

        getContentResolver().delete(
                uri,
                ArticlesDBContract.ArticleEntry.COL_TYPE + " = " + ArticlesDBContract.DB_TYPE_CATEGORY + " AND " + ArticlesDBContract.ArticleEntry.COL_TYPE_ID + " = " + categoryID,
                null);
    }

    /**
     * Loop through all the items of the collection, calling the storage method per article on that collection
     * @param collection The ArticlesCollection to attach on the given category ID
     */
    private void addNewCollectionToCategory(ArticlesCollection collection){
        String categoryID = collection.getListId();

        for (Article article : collection.getArticleList()){
            addArticleToCategory(article, categoryID);
        }
    }

    /**
     * Store an individual item
     * @param article The article to store
     * @param categoryID The category that this article will belong to
     */
    private void addArticleToCategory(Article article, String categoryID){
        ContentValues contentValues = new ContentValues();
        contentValues.put(ArticlesDBContract.ArticleEntry.COL_TYPE, ArticlesDBContract.DB_TYPE_CATEGORY);
        contentValues.put(ArticlesDBContract.ArticleEntry.COL_TYPE_ID, categoryID);

        contentValues.put(ArticlesDBContract.ArticleEntry.COL_GUID, article.getGuid());
        contentValues.put(ArticlesDBContract.ArticleEntry.COL_LINK, article.getLink());
        contentValues.put(ArticlesDBContract.ArticleEntry.COL_TITLE, article.getTitle());
        contentValues.put(ArticlesDBContract.ArticleEntry.COL_DESCRIPTION, article.getDescription());
        contentValues.put(ArticlesDBContract.ArticleEntry.COL_PUB_DATE_STR, article.getPubDateStr());
        contentValues.put(ArticlesDBContract.ArticleEntry.COL_UPDATED_STR, article.getUpdatedStr());
        contentValues.put(ArticlesDBContract.ArticleEntry.COL_PUB_DATE_GRE, article.getPubDateGre());
        contentValues.put(ArticlesDBContract.ArticleEntry.COL_IMG_THUMB, article.getImgThumbStr());
        contentValues.put(ArticlesDBContract.ArticleEntry.COL_IMG_LARGE, article.getImgLargeStr());

        Date pubDate = AppUtils.feedDate(article.getPubDateStr());
        if (pubDate!=null) { contentValues.put(ArticlesDBContract.ArticleEntry.COL_PUB_DATE, pubDate.getTime()); }
        else{ contentValues.put(ArticlesDBContract.ArticleEntry.COL_PUB_DATE, 0); }

        Date updated = AppUtils.feedDate(article.getUpdatedStr());
        if (updated!=null) { contentValues.put(ArticlesDBContract.ArticleEntry.COL_UPDATED, updated.getTime()); }
        else{ contentValues.put(ArticlesDBContract.ArticleEntry.COL_UPDATED, 0); }

        Uri uri = getContentResolver().insert(ArticlesDBContract.ArticleEntry.CONTENT_URI, contentValues);
    }
}

package eu.anifantakis.neakriti.data;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import eu.anifantakis.neakriti.data.db.ArticlesDBContract;
import eu.anifantakis.neakriti.data.feed.Article;
import eu.anifantakis.neakriti.data.feed.ArticlesCollection;

public class StorageRetrievalAsyncTask extends AsyncTask<Object, Void, ArticlesCollection> {

    public interface TaskCompleteListener {
        void onTaskComplete(ArticlesCollection collection);
    }

    private TaskCompleteListener mTaskCompleteListener;

    public StorageRetrievalAsyncTask(TaskCompleteListener listener) {
        mTaskCompleteListener = listener;
    }

    @Override
    protected ArticlesCollection doInBackground(Object... objects) {
        Context context = (Context) objects[0];
        String categoryID = (String) objects[1];
        String collectionTitle = (String) objects[2];
        Integer collectionType = (Integer) objects[3];

        //ContentResolver resolver = getco
        Cursor cursor = context.getContentResolver().query(ArticlesDBContract.ArticleEntry.CONTENT_URI,
                null,
                ArticlesDBContract.ArticleEntry.COL_TYPE + " = " + collectionType + " AND " + ArticlesDBContract.ArticleEntry.COL_TYPE_ID + " = " + categoryID,
                null,
                ArticlesDBContract.ArticleEntry._ID + " DESC"
        );

        if (cursor!=null) {
            if (cursor.getCount() > 0) {
                ArticlesCollection collection = new ArticlesCollection(collectionTitle, collectionType, categoryID);

                cursor.moveToFirst();
                while (!cursor.isAfterLast()){
                    Article article = new Article();

                    article.setGuid(cursor.getInt(cursor.getColumnIndex(ArticlesDBContract.ArticleEntry.COL_GUID)));
                    article.setLink(cursor.getString(cursor.getColumnIndex(ArticlesDBContract.ArticleEntry.COL_LINK)));
                    article.setTitle(cursor.getString(cursor.getColumnIndex(ArticlesDBContract.ArticleEntry.COL_TITLE)));
                    article.setDescription(cursor.getString(cursor.getColumnIndex(ArticlesDBContract.ArticleEntry.COL_DESCRIPTION)));
                    article.setPubDateStr(cursor.getString(cursor.getColumnIndex(ArticlesDBContract.ArticleEntry.COL_PUB_DATE_STR)));
                    article.setUpdatedStr(cursor.getString(cursor.getColumnIndex(ArticlesDBContract.ArticleEntry.COL_UPDATED_STR)));
                    article.setPubDateGre(cursor.getString(cursor.getColumnIndex(ArticlesDBContract.ArticleEntry.COL_PUB_DATE_GRE)));
                    article.setImgThumb(cursor.getString(cursor.getColumnIndex(ArticlesDBContract.ArticleEntry.COL_IMG_THUMB)));
                    article.setImgLarge(cursor.getString(cursor.getColumnIndex(ArticlesDBContract.ArticleEntry.COL_IMG_LARGE)));

                    collection.addArticle(article);
                    cursor.moveToNext();
                }
                // all results of the favorites are contained in a single page (no endless load here)
                return collection;
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(ArticlesCollection collection) {
        super.onPostExecute(collection);
        //if (collection != null) {
            mTaskCompleteListener.onTaskComplete(collection);
        //}
    }
}

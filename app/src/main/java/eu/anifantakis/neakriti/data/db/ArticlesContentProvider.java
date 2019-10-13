package eu.anifantakis.neakriti.data.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static eu.anifantakis.neakriti.data.db.ArticlesDBContract.ArticleEntry.TABLE_NAME;

public class ArticlesContentProvider extends ContentProvider{

    public static final int ARTICLES = 100;
    public static final int ARTICLE_WITH_ID = 101;
    public static final int CATEGORY_WITH_ID = 102;
    public static final int FAVORITE_ARTICLE_WITH_ID = 103;

    ArticlesDBHelper dbHelper;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(ArticlesDBContract.AUTHORITY, ArticlesDBContract.PATH_ARTICLES, ARTICLES);
        uriMatcher.addURI(ArticlesDBContract.AUTHORITY, ArticlesDBContract.PATH_ARTICLES + "/#", ARTICLE_WITH_ID);
        uriMatcher.addURI(ArticlesDBContract.AUTHORITY, ArticlesDBContract.PATH_CATEGORY + "/#", CATEGORY_WITH_ID);
        uriMatcher.addURI(ArticlesDBContract.AUTHORITY, ArticlesDBContract.PATH_FAVORITE_ARTICLES + "/#", FAVORITE_ARTICLE_WITH_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        dbHelper = new ArticlesDBHelper(context);
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor retCursor;

        switch (match){
            case ARTICLES:
                retCursor =  db.query(TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        Uri retUri;

        if (match == ARTICLES) {
            long id = db.insert(TABLE_NAME, null, contentValues);
            if (id > 0) {
                retUri = ContentUris.withAppendedId(ArticlesDBContract.ArticleEntry.CONTENT_URI, id);
            } else {
                throw new SQLException("Failed to insert row into " + uri);
            }
        } else {
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return retUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int tasksDeleted;

        switch (match){
            case ARTICLE_WITH_ID:
                String id = uri.getPathSegments().get(1);
                tasksDeleted = db.delete(TABLE_NAME, ArticlesDBContract.ArticleEntry.COL_GUID+ "=?", new String[]{id});
                break;

            case FAVORITE_ARTICLE_WITH_ID:
                String favorite_article_id = uri.getPathSegments().get(1);
                tasksDeleted = db.delete(TABLE_NAME, ArticlesDBContract.ArticleEntry.COL_TYPE + " = " + ArticlesDBContract.DB_TYPE_FAVORITE + " AND " + ArticlesDBContract.ArticleEntry.COL_GUID+ "=?", new String[]{favorite_article_id});
                break;

            case CATEGORY_WITH_ID:
                String categoryID = uri.getPathSegments().get(1);
                tasksDeleted = db.delete(TABLE_NAME,
                        ArticlesDBContract.ArticleEntry.COL_TYPE+"="+ArticlesDBContract.DB_TYPE_CATEGORY+
                                " AND "+ ArticlesDBContract.ArticleEntry.COL_TYPE_ID+ "=?", new String[]{categoryID});
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return tasksDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}

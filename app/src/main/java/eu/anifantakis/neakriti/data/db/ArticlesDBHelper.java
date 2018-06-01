package eu.anifantakis.neakriti.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import eu.anifantakis.neakriti.data.db.ArticlesDBContract.ArticleEntry;

public class ArticlesDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "neakriti.db";
    private static final int DATABASE_VERSION = 2;

    ArticlesDBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_ARTICLES_TABLE =
                "CREATE TABLE "+ArticleEntry.TABLE_NAME + "(" +
                        ArticleEntry._ID +                  " INTEGER PRIMARY KEY AUTOINCREMENT, "+
                        ArticleEntry.COL_TYPE +             " INTEGER NOT NULL, "+
                        ArticleEntry.COL_TYPE_ID +          " TEXT NOT NULL, "+
                        ArticleEntry.COL_GUID +             " INTEGER NOT NULL, "+
                        ArticleEntry.COL_LINK +             " TEXT NOT NULL, "+
                        ArticleEntry.COL_TITLE +            " TEXT NOT NULL, "+
                        ArticleEntry.COL_DESCRIPTION +      " TEXT NOT NULL, "+
                        ArticleEntry.COL_PUB_DATE_STR +     " TEXT NOT NULL, "+
                        ArticleEntry.COL_PUB_DATE +         " TIMESTAMP NOT NULL, "+
                        ArticleEntry.COL_UPDATED_STR +      " TEXT, "+
                        ArticleEntry.COL_UPDATED +          " TIMESTAMP, "+
                        ArticleEntry.COL_PUB_DATE_GRE +     " TEXT NOT NULL, "+
                        ArticleEntry.COL_IMG_THUMB +        " TEXT, "+
                        ArticleEntry.COL_IMG_LARGE +        " TEXT, "+
                        ArticleEntry.COL_IMG_BLOB_THUMB +   " BLOB, "+
                        ArticleEntry.COL_IMG_BLOB_LARGE +   " BLOB "+
                 ")";
        sqLiteDatabase.execSQL(SQL_CREATE_ARTICLES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ArticleEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
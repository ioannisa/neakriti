package eu.anifantakis.neakriti.data.db;

import android.net.Uri;
import android.provider.BaseColumns;

public final class ArticlesDBContract {
    /**
     * No instances of the contract class are allowed
     */
    private ArticlesDBContract() {}

    public static final String AUTHORITY = "eu.anifantakis.neakriti";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_ARTICLES = "articles";

    public static class ArticleEntry implements BaseColumns{
        // Content URI for the Content Provider of a single article
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_ARTICLES).build();

        // Fields regarding the table "articles" and its columns
        public static final String TABLE_NAME = "article";

        // Types: 1=Category, 2=Zone, 3=Tag, 4=Favorite
        public static final String COL_TYPE = "type";
        public static final String COL_TYPE_ID = "type_id";

        public static final String COL_GUID = "guid";
        public static final String COL_LINK = "link";
        public static final String COL_TITLE = "title";
        public static final String COL_DESCRIPTION = "description";
        public static final String COL_PUB_DATE_STR = "pubDateStr";
        public static final String COL_UPDATED_STR = "updatedStr";
        public static final String COL_PUB_DATE = "pubDate";
        public static final String COL_UPDATED = "updated";
        public static final String COL_PUB_DATE_GRE = "pubDateGre";
        public static final String COL_IMG_THUMB = "img_thumb";
        public static final String COL_IMG_LARGE = "img_large";
    }
}

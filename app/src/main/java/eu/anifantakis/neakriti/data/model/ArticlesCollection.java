package eu.anifantakis.neakriti.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ArticlesCollection {
    public static final int LISTY_TYPE_CATEGORY = 0;
    public static final int LISTY_TYPE_TAG = 1;
    public static final int LISTY_TYPE_ZONE = 2;
    public static final int LISTY_TYPE_SEARCH = 3;
    public static final int LISTY_TYPE_FAVORITE = 4;

    private String listName;
    private int listType;
    private int listId;
    private List<Article> articleList;

    public ArticlesCollection() {
        articleList = new ArrayList<>();
    }

    public ArticlesCollection(List<Article> articlesList) {
        articleList = articlesList;
    }

    public int addArticle(Article article) {
        articleList.add(article);
        return articleList.size();
    }

    public void setArticlesList(List<Article> articlesList){
        this.articleList=articlesList;
    }

    public void clear(){
        articleList.clear();
    }

    public Article getArticle(int location) {
        return articleList.get(location);
    }

    public int getCollectionSize() {
        return articleList.size();
    }
}

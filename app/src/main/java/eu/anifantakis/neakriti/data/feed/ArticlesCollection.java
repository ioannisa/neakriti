package eu.anifantakis.neakriti.data.feed;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class ArticlesCollection implements Parcelable {
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

    protected ArticlesCollection(Parcel in) {
        listName = in.readString();
        listType = in.readInt();
        listId = in.readInt();
        articleList = in.createTypedArrayList(Article.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(listName);
        dest.writeInt(listType);
        dest.writeInt(listId);
        dest.writeTypedList(articleList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ArticlesCollection> CREATOR = new Creator<ArticlesCollection>() {
        @Override
        public ArticlesCollection createFromParcel(Parcel in) {
            return new ArticlesCollection(in);
        }

        @Override
        public ArticlesCollection[] newArray(int size) {
            return new ArticlesCollection[size];
        }
    };

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

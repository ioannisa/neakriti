package eu.anifantakis.neakriti.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Articles {
    public static final int LISTY_TYPE_CATEGORY = 0;
    public static final int LISTY_TYPE_TAG = 1;
    public static final int LISTY_TYPE_ZONE = 2;
    public static final int LISTY_TYPE_SEARCH = 3;
    public static final int LISTY_TYPE_FAVORITE = 4;

    private String listName;
    private int listType;
    private int listId;
    private List<Article> articleList;

    public Articles() {
        articleList = new ArrayList<>();
    }

    public int addArticle(Article article) {
        articleList.add(article);
        return articleList.size();
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

    public static class Article implements Parcelable {
        private int guid;
        private String link;
        private String title;
        private String description;
        private Date pubDate;
        private Date updated;
        private String pubDateGre;
        private List<String> enclosures;


        public int getGuid() {
            return guid;
        }

        public void setGuid(int guid) {
            this.guid = guid;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Date getPubDate() {
            return pubDate;
        }

        public void setPubDate(Date pubDate) {
            this.pubDate = pubDate;
        }

        public Date getUpdated() {
            return updated;
        }

        public void setUpdated(Date updated) {
            this.updated = updated;
        }

        public String getPubDateGre() {
            return pubDateGre;
        }

        public void setPubDateGre(String pubDateGre) {
            this.pubDateGre = pubDateGre;
        }

        public List<String> getEnclosures() {
            return enclosures;
        }

        public void setEnclosures(List<String> enclosures) {
            this.enclosures = enclosures;
        }

        protected Article(Parcel in) {
            guid = in.readInt();
            link = in.readString();
            title = in.readString();
            description = in.readString();

            long pubDateLong = in.readLong();
            if (pubDateLong>0) { pubDate = new Date(pubDateLong); } else{ pubDate = null; }

            long updatedLong = in.readLong();
            if (updatedLong>0) { updated = new Date(updatedLong); } else{ updated = null; }

            pubDateGre = in.readString();
            enclosures = in.createStringArrayList();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(guid);
            dest.writeString(link);
            dest.writeString(title);
            dest.writeString(description);
            if (pubDate != null) { dest.writeLong( pubDate.getTime()); } else dest.writeLong(0);
            if (updated != null) { dest.writeLong( updated.getTime()); } else dest.writeLong(0);
            dest.writeString(pubDateGre);
            dest.writeStringList(enclosures);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Parcelable.Creator<Article> CREATOR = new Parcelable.Creator<Article>() {
            @Override
            public Article createFromParcel(Parcel in) {
                return new Article(in);
            }

            @Override
            public Article[] newArray(int size) {
                return new Article[size];
            }
        };
    }
}

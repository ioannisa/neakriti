package eu.anifantakis.neakriti.data.feed.gson;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Article implements Parcelable {
    private int guid;
    private String link;
    private String title;
    private String description;
    private String pubDate;
    private String updated;
    private String pubDateGre;

    private List<String> enclosures;

    @SerializedName("img_thumb")
    private ArticleImg imgThumb;

    @SerializedName("img_large")
    private ArticleImg imgLarge;

    private String groupName;


    public Article() {
    }

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

    public ArticleImg getImgThumb() {
        return imgThumb;
    }

    public String getImgThumbStr(){
        String result;
        try {
            result = imgThumb.getAttributes().getUrl();
            if (result == null)
                result = "";
        }
        catch (Exception e){ result = ""; }
        return result;
    }

    public void setImgThumb(ArticleImg imgThumb) {
        this.imgThumb = imgThumb;
    }

    public void setImgThumb(String imgThumb) {
        this.imgThumb = new ArticleImg(imgThumb);

    }

    public ArticleImg getImgLarge() {
        return imgLarge;
    }

    public String getImgLargeStr(){
        String result;
        try {
            result = imgLarge.getAttributes().getUrl();
            if (result == null)
                result = "";
        }
        catch (Exception e){ result = ""; }
        return result;
    }

    public void setImgLarge(ArticleImg imgLarge) {
        this.imgLarge = imgLarge;
    }

    public void setImgLarge(String imgLarge){
        this.imgLarge = new ArticleImg(imgLarge);
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setPubDateStr(String pubDateStr) {
        this.pubDate = pubDateStr;
    }

    public String getPubDateStr() {
        return pubDate;
    }

    public void setUpdatedStr(String updatedStr) {
        this.updated = updatedStr;
    }

    public String getUpdatedStr() {
        return this.updated;
    }

    protected Article(Parcel in) {
        guid = in.readInt();
        link = in.readString();
        title = in.readString();
        description = in.readString();
        pubDate = in.readString();
        updated = in.readString();
        pubDateGre = in.readString();
        enclosures = in.createStringArrayList();
        imgThumb = in.readParcelable(ArticleImg.class.getClassLoader());
        imgLarge = in.readParcelable(ArticleImg.class.getClassLoader());
        groupName = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(guid);
        dest.writeString(link);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(pubDate);
        dest.writeString(updated);
        dest.writeString(pubDateGre);
        dest.writeStringList(enclosures);
        dest.writeParcelable(imgThumb, flags);
        dest.writeParcelable(imgLarge, flags);
        dest.writeString(groupName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Article> CREATOR = new Creator<Article>() {
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
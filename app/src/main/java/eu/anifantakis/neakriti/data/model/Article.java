package eu.anifantakis.neakriti.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Commit;

import java.util.Date;
import java.util.List;

import eu.anifantakis.neakriti.utils.AppUtils;

@Root(name = "item", strict = false)
public class Article implements Parcelable {

    @Element(name = "guid", required = false)
    private int guid;

    @Element(name = "link", required = false)
    private String link;

    @Element(name = "title", required = false)
    private String title;

    @Element(name = "description", required = false)
    private String description;

    @Element(name = "pubDate", required = false)
    private String pubDateStr;

    @Element(name = "updated", required = false)
    private String updatedStr;

    @Element(name = "pubDateGre", required = false)
    private String pubDateGre;

    private List<String> enclosures;

    @Element(name = "img_thumb", required = false)
    private RssImgThumb imgThumbObj;
    private String imgThumb;

    @Element(name = "img_large", required = false)
    private RssImgThumb imgLargeObj;
    private String imgLarge;

    private String groupName;

    @Commit
    private void parseDates(){
        if (imgThumbObj!=null) {
            imgThumb = imgThumbObj.getUrl();
            imgLarge = imgLargeObj.getUrl();
        }
    }

    public Article(){}

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

    public String getImgThumb() {
        return imgThumb;
    }

    public void setImgThumb(String imgThumb) {
        this.imgThumb = imgThumb;
    }

    public String getImgLarge() {
        return imgLarge;
    }

    public void setImgLarge(String imgLarge) {
        this.imgLarge = imgLarge;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setPubDateStr(String pubDateStr){
        this.pubDateStr = pubDateStr;
    }

    public String getPubDateStr(){
        return pubDateStr;
    }

    public void setUpdatedStr(String updatedStr){
        this.updatedStr = updatedStr;
    }

    public String getUpdatedStr(){
        return this.updatedStr;
    }

    protected Article(Parcel in) {
        guid = in.readInt();
        link = in.readString();
        title = in.readString();
        description = in.readString();
        pubDateStr = in.readString();
        updatedStr = in.readString();
        pubDateGre = in.readString();
        enclosures = in.createStringArrayList();
        imgThumb = in.readString();
        imgLarge = in.readString();
        groupName = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(guid);
        dest.writeString(link);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(pubDateStr);
        dest.writeString(updatedStr);
        dest.writeString(pubDateGre);
        dest.writeStringList(enclosures);
        dest.writeString(imgThumb);
        dest.writeString(imgLarge);
        dest.writeString(groupName);
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
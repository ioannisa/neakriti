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

@Root(name = "item", strict = false)
public class Article implements Parcelable {

    private int guid;

    @Element(name = "link", required = false)
    private String link;

    @Element(name = "title", required = false)
    private String title;

    @Element(name = "description", required = false)
    private String description;

    @Element(name = "pubDate", required = false)
    private String pubDateStr;
    private Date pubDate;

    @Element(name = "updated", required = false)
    private String updatedStr;
    private Date updated;

    @Element(name = "pubDateGre", required = false)
    private String pubDateGre;

    private List<String> enclosures;



    // sample: <img_thumb url="https://s1.neakriti.gr/images/360x254/files/Image/201704/doc_20170427_1784929_bretania.jpg" length="10000" type="image/jpeg"/>
    @Root(name = "img_thumb")
    public class ImgThumb{
        @Attribute(name = "url")
        public String imgThumb;
    }
    private String imgThumb;


    // sample: <img_large url="https://s1.neakriti.gr/images/1542x770/files/Image/201704/doc_20170427_1784929_bretania.jpg" length="10000" type="image/jpeg"/>
    @Element(name = "img_large", required = false)
    @Attribute(name = "url")
    private String imgLarge;

    @Commit
    private void parseDates(){
        if (pubDateStr != null){
            // TODO Use a converter to store date representation from the date string
        }

        // if I can get imgThumb populated correctly here in my @Commit section, the app will show it
        // comment out the bellow line to see that working - thing is I don't know how to populate it yet... :(
        //imgThumb = "https://s1.neakriti.gr/images/360x254/files/Image/201704/doc_20170427_1784929_bretania.jpg";
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

    protected Article(Parcel in) {
        guid = in.readInt();
        link = in.readString();
        title = in.readString();
        description = in.readString();

        pubDateStr = in.readString();
        long pubDateLong = in.readLong();
        if (pubDateLong>0) { pubDate = new Date(pubDateLong); } else{ pubDate = null; }

        updatedStr = in.readString();
        long updatedLong = in.readLong();
        if (updatedLong>0) { updated = new Date(updatedLong); } else{ updated = null; }

        pubDateGre = in.readString();
        enclosures = in.createStringArrayList();
        imgThumb = in.readString();
        imgLarge = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(guid);
        dest.writeString(link);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(pubDateStr); if (pubDate != null) { dest.writeLong( pubDate.getTime()); } else dest.writeLong(0);
        dest.writeString(updatedStr); if (updated != null) { dest.writeLong( updated.getTime()); } else dest.writeLong(0);
        dest.writeString(pubDateGre);
        dest.writeStringList(enclosures);
        dest.writeString(imgThumb);
        dest.writeString(imgLarge);
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
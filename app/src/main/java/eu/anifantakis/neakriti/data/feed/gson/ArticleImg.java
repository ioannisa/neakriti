package eu.anifantakis.neakriti.data.feed.gson;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class ArticleImg implements Parcelable {
    @SerializedName("@attributes")
    private ImgAttributes attributes;

    private ArticleImg(Parcel in) {
        attributes = in.readParcelable(ImgAttributes.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(attributes, flags);
    }

    ArticleImg(String imgUrl) {
        attributes = new ImgAttributes(imgUrl);
    }

    public static final Creator<ArticleImg> CREATOR = new Creator<ArticleImg>() {
        @Override
        public ArticleImg createFromParcel(Parcel in) {
            return new ArticleImg(in);
        }

        @Override
        public ArticleImg[] newArray(int size) {
            return new ArticleImg[size];
        }
    };

    ImgAttributes getAttributes() {
        return attributes;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}

package eu.anifantakis.neakriti.data.feed;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "img_thumb", strict = false)
public class RssImgThumb {

    @Attribute
    private String url;

    @Attribute
    private String length;

    @Attribute
    private String type;

    public String getUrl() {
        return url;
    }

    public String getLength(){
        return length;
    }

    public String getType(){
        return type;
    }
}

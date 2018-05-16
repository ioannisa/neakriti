package eu.anifantakis.neakriti.data.feed;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(strict = false)
public class RssChannel {
    @ElementList(name = "item", required = true, inline = true)
    private List<Article> itemList;

    public List<Article> getItemList() {
        return itemList;
    }

    public void setItemList(List<Article> itemList) {
        this.itemList = itemList;
    }

    public RssChannel(List<Article> mFeedItems) {
        this.itemList = mFeedItems;
    }

    public RssChannel() {
    }
}

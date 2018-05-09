package eu.anifantakis.neakriti.data.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "rss", strict = false)
public class RssFeed {

    @Attribute
    private
    String version;

    @Element
    private
    RssChannel channel;

    public RssChannel getChannel() {
        return channel;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setChannel(RssChannel channel) {
        this.channel = channel;
    }
}

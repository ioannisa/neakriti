package eu.anifantakis.neakriti.data.feed.gson;

import java.util.ArrayList;
import java.util.List;

public class Channel {
    private String title;
    private ArrayList<Article> item;

    public ArrayList<Article> getItems(){
        return item;
    }
}

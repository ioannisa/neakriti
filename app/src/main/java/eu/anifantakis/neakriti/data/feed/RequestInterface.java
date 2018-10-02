package eu.anifantakis.neakriti.data.feed;

import eu.anifantakis.neakriti.data.feed.gson.Feed;
import eu.anifantakis.neakriti.utils.AppUtils;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by ioannisa on 24/3/2018.
 */
public interface RequestInterface {
    @GET(AppUtils.RSSFEED_BASE)
    Call<Feed> getFeedByCategory(@Query("srvid") String srvid, @Query("items") int items);

    @GET(AppUtils.RSSFEED_BASE)
    Call<Feed> getFeedByTag(@Query("tagid") String srvid, @Query("items") int items);

    @GET(AppUtils.RSSFEED_BASE)
    Call<Feed> getFeedByArticleId(@Query("docid") String docid);
}

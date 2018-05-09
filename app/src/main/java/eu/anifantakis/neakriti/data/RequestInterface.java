package eu.anifantakis.neakriti.data;

import eu.anifantakis.neakriti.data.model.RssFeed;
import eu.anifantakis.neakriti.utils.AppUtils;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by ioannisa on 24/3/2018.
 */
public interface RequestInterface {

    @GET(AppUtils.XML_LOC)
    Call<RssFeed> getFeedByCategory(@Query("srvid") String srvid, @Query("items") int items);
    Call<RssFeed> getFeedByTag(@Query("tagid") String srvid, @Query("items") int items);
}

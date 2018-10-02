package eu.anifantakis.neakriti.data.facebook_comments;

import eu.anifantakis.neakriti.data.facebook_comments.Feed;
import eu.anifantakis.neakriti.utils.AppUtils;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RequestFacebookCommentsCountInterface {
    @GET(AppUtils.URL_FACEBOOK_GRAPH)
    Call<Feed>  getCountByArticleURL(@Query("id") String url);
}

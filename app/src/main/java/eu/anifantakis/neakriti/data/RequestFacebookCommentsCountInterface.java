package eu.anifantakis.neakriti.data;

import eu.anifantakis.neakriti.data.facebook_comments.Feed;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RequestFacebookCommentsCountInterface {
    @GET("https://graph.facebook.com")
    Call<Feed>  getCountByArticleURL(@Query("id") String url);
}

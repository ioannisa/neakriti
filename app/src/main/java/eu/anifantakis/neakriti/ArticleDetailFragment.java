package eu.anifantakis.neakriti;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import eu.anifantakis.neakriti.data.model.Article;
import eu.anifantakis.neakriti.utils.AppUtils;

/**
 * A fragment representing a single Article detail screen.
 * This fragment is either contained in a {@link ArticleListActivity}
 * in two-pane mode (on tablets) or a {@link ArticleDetailActivity}
 * on handsets.
 */
public class ArticleDetailFragment extends Fragment {
    private Article mArticle;
    private WebView mWebView;
    private AdView adView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments().containsKey(AppUtils.EXTRAS_ARTICLE)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mArticle = getArguments().getParcelable(AppUtils.EXTRAS_ARTICLE);

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                // using a theme with NoActionBar we would set title like that
                //appBarLayout.setTitle(mArticle.getTitle());
                //appBarLayout.setTitle(mArticle.getGroupName());

                // using a theme with ActionBar we set the activity's toolbar name like that
                getActivity().setTitle(mArticle.getGroupName());
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.article_detail, container, false);

        TextView detailTitle = (TextView) rootView.findViewById(R.id.detail_title);
        detailTitle.setText(mArticle.getTitle());

        TextView detailDate = (TextView) rootView.findViewById(R.id.detail_date);
        detailDate.setText(AppUtils.pubDateFormat(mArticle.getPubDateStr()));

                mWebView = (WebView) rootView.findViewById(R.id.article_detail);
        WebSettings webSettings;
        webSettings = mWebView.getSettings();

        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        mWebView.getSettings().setAllowFileAccess(true);
        webSettings.setAppCacheEnabled(true);

        // Show the dummy content as text in a TextView.
        if (mArticle != null) {
            Log.d("LOADING WEB VIEW", "ARTICLE IS NOT NULL");

            String theStory = "";
            theStory =  mArticle.getDescription();

            theStory = theStory.replace("=\"//www.", "=\"https://www.");
            theStory = theStory.replace("src=\"//", "src=\"https://");
            theStory = theStory.replace("=\"/", "=\"https://www.neakriti.gr/");
            theStory.replace("with=\"620\"", "width=\"100%\"");
            String basicWebStory = theStory.replace(Character.toString((char)10), "<br/>");
            String webStory   = "<div id='story' class='story'>"+basicWebStory+"</div>";

            boolean day = true;
            String dayNightStyle = "";
            if (day = false) {
                dayNightStyle = "body,p,div{background:#333333 !Important; color:#eeeeee;} a{color:#ee3333 !Important}";
            }
            webStory   = "<!DOCTYPE html><html lang='el'><head><title>"+mArticle.getTitle()+"</title> <meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\"/> <link rel='canonical' href='"+mArticle.getLink()+"'> <link href='https://fonts.googleapis.com/css?family=Roboto:400,700,900&subset=greek,latin' rel='stylesheet' type='text/css'> <style> a{ text-decoration:none; color:#a00; font-weight:600; } body{line-height:normal; font-family:'Roboto'; padding-bottom:50px;}                  object,img,iframe,div,video,param,embed{max-width: 100%; }"+dayNightStyle+"</style></head><body>"+webStory+"</body></html>";

            mWebView.loadDataWithBaseURL(null, webStory, "text/html", "UTF-8", null);


            adView = (AdView) rootView.findViewById(R.id.adView);

            //((TextView) rootView.findViewById(R.id.article_detail_text)).setText(webStory);
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // fetch google ads
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail_menu, menu);
    }
}

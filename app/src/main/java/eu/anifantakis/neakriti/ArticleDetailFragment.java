package eu.anifantakis.neakriti;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import eu.anifantakis.neakriti.data.db.ArticlesDBContract;
import eu.anifantakis.neakriti.data.facebook_comments.Feed;
import eu.anifantakis.neakriti.data.facebook_comments.RequestFacebookCommentsCountInterface;
import eu.anifantakis.neakriti.data.feed.gson.Article;
import eu.anifantakis.neakriti.databinding.ActivityArticleDetailBinding;
import eu.anifantakis.neakriti.databinding.FragmentArticleDetailBinding;
import eu.anifantakis.neakriti.utils.AppUtils;
import eu.anifantakis.neakriti.utils.NeaKritiApp;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static eu.anifantakis.neakriti.utils.AppUtils.dipToPixels;
import static eu.anifantakis.neakriti.utils.AppUtils.isNetworkAvailable;
import static eu.anifantakis.neakriti.utils.AppUtils.isNightMode;
import static eu.anifantakis.neakriti.utils.AppUtils.onlineMode;
import static eu.anifantakis.neakriti.utils.AppUtils.spec;
import static eu.anifantakis.neakriti.utils.NeaKritiApp.sharedPreferences;

/**
 * A fragment representing a single Article detail screen.
 * This fragment is either contained in a {@link ArticleListActivity}
 * in two-pane mode (on tablets) or a {@link ArticleDetailActivity}
 * on handsets.
 */
public class ArticleDetailFragment extends Fragment implements TextToSpeech.OnInitListener {
    private FragmentArticleDetailBinding binding;
    private Article mArticle;
    private TextToSpeech mTextToSpeech;
    private Tracker mTracker;
    private WebSettings webSettings;
    private WebView mWebView;
    private AdView adView;
    private RelativeLayout footerlayout;
    private CollapsingToolbarLayout appBarLayout;
    private ActivityArticleDetailBinding activityBinding;

    private FrameLayout mContainer;
    private WebView mWebViewComments;
    private WebView mWebviewPop;
    private FirebaseAnalytics mFirebaseAnalytics;

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

        // initialize TTS
        mTextToSpeech = new TextToSpeech(getActivity(), this);

        mTracker = ((NeaKritiApp) Objects.requireNonNull(getActivity()).getApplication()).getDefaultTracker();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(Objects.requireNonNull(getContext()));

        assert getArguments() != null;
        if (getArguments().containsKey(AppUtils.EXTRAS_ARTICLE)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mArticle = getArguments().getParcelable(AppUtils.EXTRAS_ARTICLE);
        }

        Activity activity = this.getActivity();
        appBarLayout = activity.findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            // using a theme with NoActionBar we would set title like that (CASE 1)
            //appBarLayout.setTitle(mArticle.getTitle());

            // using a theme with ActionBar we set the activity's toolbar name like that (CASE 2)
            //getActivity().setTitle(mArticle.getGroupName());

            // So we are applying the NoActionBar code here (aka CASE 1).
            appBarLayout.setTitle(mArticle.getGroupName());
        }
    }

    /**
     * Retain Article object during device rotation
     * @param outState the bundle to save
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(AppUtils.EXTRAS_ARTICLE, mArticle);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_article_detail, container, false);
        final View rootView = binding.getRoot();
        TextView detailTitle = binding.detailTitle;
        detailTitle.setText(mArticle.getTitle());

        TextView detailDate = binding.detailDate;
        detailDate.setText(AppUtils.pubDateFormat(mArticle.getPubDateStr()));

        // get the activity's public binding to associate controls with the activity's footer
        // we use try/catch as the "quick-settings" bottom menu refers only to the detail activity and will produce
        // exception when called in landscape-mode of tablet version of the app (from this fragment not belonging to the ArticleDetailActivity)
        try {
            activityBinding = ((ArticleDetailActivity) Objects.requireNonNull(getActivity())).binding;
            footerlayout = activityBinding.incQuickSettings.footerLayout;
            ImageButton btnZoomIn = activityBinding.incQuickSettings.btnzoomin;
            ImageButton btnZoomOut = activityBinding.incQuickSettings.btnzoomout;
            AppCompatCheckBox ckboxLinespace = activityBinding.incQuickSettings.ckboxLinespace;
            //AppCompatCheckBox ckboxNightMode = activityBinding.incQuickSettings.ckboxNightmode;

            btnZoomIn.setOnClickListener(view -> changeFontSize(true));

            btnZoomOut.setOnClickListener(view -> changeFontSize(false));

            // setup quick menu options based on the shared preferences
            Thread t = new Thread(() -> {
                if (sharedPreferences == null) {
                    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                }
                activityBinding.incQuickSettings.ckboxNightmode.setChecked(sharedPreferences.getBoolean(getString(R.string.pref_night_reading_key), false));
                activityBinding.incQuickSettings.ckboxLinespace.setChecked(sharedPreferences.getBoolean(getString(R.string.pref_increased_line_distance_key), true));

                //setZoomButtonsEnabled();

                ((ArticleDetailActivity)getActivity()).initializatioin = false;
            });
            t.run();


            ckboxLinespace.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                if (!((ArticleDetailActivity)getActivity()).initializatioin) {

                    SharedPreferences.Editor editor1 = sharedPreferences.edit();
                    editor1.putBoolean(getString(R.string.pref_increased_line_distance_key), isChecked);
                    editor1.apply();

                    displayArticle();
                }
            });

        }catch (Exception e){}

        // comments section
        mWebViewComments = binding.commentsView;
        mContainer = binding.webviewFrame;

        // article section
        mWebView = binding.articleDetail;

        webSettings = mWebView.getSettings();
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);

        mWebView.getSettings().setAllowFileAccess(true);
        webSettings.setAppCacheEnabled(true);

        CookieManager.getInstance().setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= 21) {
            mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView, true);
        }

        // set the webview scrollbar to be "inside" to avoid unecessary "right-side" padding space
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                // Once the article is done loading, load the comments section if we are in online mode
                if (onlineMode){
                    // disabled for now... will re-enable comments functionality in future version

                    // TODO: RE-ENABLE FACEBOOK COMMENTS
                    //loadComments();
                }
            }
        });

        adView = binding.adView;

        /*
        adView.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();

                // This is a working hack...
                // The advert loading causes undesired scrolling.  To solve this we disable focusable descendants in xml
                // however disabling focusable descendants, does not allow to focus on the facebook comments section and write your comment.
                // so when the advert is done displaying the advert, its now safe to re-enable focusable descendants without undesirable scrolls.

                // TODO: RE-ENABLE FACEBOOK COMMENTS
                // disabling hack in order to revisit it in future version - will remove facbook comments all together in the existing version.
                //binding.articleContainer.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
            }
        });
        */

        // Handling Rotation
        if (savedInstanceState!=null){
            if (savedInstanceState.containsKey(AppUtils.EXTRAS_ARTICLE)) {
                mArticle = savedInstanceState.getParcelable(AppUtils.EXTRAS_ARTICLE);
            }
        }
        Activity activity = this.getActivity();
        assert activity != null;
        appBarLayout = activity.findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(mArticle.getGroupName());
        }

        // Show the dummy content as text in a TextView.
        if (mArticle != null) {
            scaleFontSize();
            displayArticle();
            displayAdverts();
        }

        // get the number of article comments using the Facebook API.
        // TODO: RE-ENABLE FACEBOOK COMMENTS
        //pullCommentsCount();

        return rootView;
    }

    /**
     * Get the amount of comments the current article has, and put it on the special FAB control that can display numbers (similar to notifications count)
     */
    private void pullCommentsCount(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppUtils.URL_FACEBOOK_GRAPH)
                .client(new OkHttpClient.Builder().connectionSpecs(Collections.singletonList(spec)).build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestFacebookCommentsCountInterface request = retrofit.create(RequestFacebookCommentsCountInterface.class);
        Call<Feed> call = request.getCountByArticleURL(mArticle.getLink());
        call.enqueue(new Callback<Feed>() {
            @Override
            public void onResponse(@NonNull Call<Feed> call, @NonNull Response<Feed> response) {
                assert response.body() != null;
                int count = response.body().getShare().getCommentCount();
                Log.d("FACEBOOK", "COUNT: "+count);

                //binding.counterFab.setCount(count);
            }

            @Override
            public void onFailure(@NonNull Call<Feed> call, @NonNull Throwable t) {
                Log.d("FACEBOOK", "FETCH FAIL");
                //binding.counterFab.setCount(0);
            }
        });
    }

    /**
     * Sets the zoom in and zoom out quick settings buttons to enabled/disabled depending on zoom value
     */
    private void setZoomButtonsEnabled(){
        int scaleFont = sharedPreferences.getInt(getString(R.string.pref_font_size_key), 1);
        if (scaleFont==0){
            activityBinding.incQuickSettings.btnzoomin.setEnabled(false);
            activityBinding.incQuickSettings.btnzoomout.setEnabled(true);
        }
        else if (scaleFont==4){
            activityBinding.incQuickSettings.btnzoomin.setEnabled(true);
            activityBinding.incQuickSettings.btnzoomout.setEnabled(false);
        }
        else{
            activityBinding.incQuickSettings.btnzoomin.setEnabled(true);
            activityBinding.incQuickSettings.btnzoomout.setEnabled(true);
        }
    }

    private void displayAdverts(){
        // show adverts if we are online
        if (onlineMode) {
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }
    }

    /**
     * Change font size in quick settings (either zoom in or zoom out)
     * @param increase if true a zoom-in is requested, otherwise zoom-out
     */
    @SuppressLint("ApplySharedPref")
    private void changeFontSize(boolean increase){
        int scaleFont = scaleFonts(sharedPreferences.getInt(getString(R.string.pref_font_size_key), 1));
        int newFontValue = scaleFont;

        int[] fontScaleArray = {0, 1, 2, 3, 4};
        boolean applyChange = false;

        if (scaleFont<4 && increase) {
            applyChange = true;
            newFontValue = scaleFont+1;
        }
        else if (scaleFont>0 && !increase){
            applyChange = true;
            newFontValue = scaleFont-1;
        }

        if (applyChange){
            SharedPreferences.Editor editor1 = sharedPreferences.edit();
            editor1.putInt(getString(R.string.pref_font_size_key), fontScaleArray[newFontValue]);
            editor1.commit();

            displayArticle();
        }
    }

    private int scaleFonts(int fontscale){
        if (fontscale==0){
            webSettings.setDefaultFontSize(15);
            //datetime.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float)15);
            return 0;
        }
        else if (fontscale==1){
            webSettings.setDefaultFontSize(18);
            //datetime.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float)18);
            return 1;
        }
        else if (fontscale==2){
            webSettings.setDefaultFontSize(21);
            //datetime.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float)21);
            return 2;
        }

        else if (fontscale==3){
            webSettings.setDefaultFontSize(21);
            //datetime.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float)25);
            return 3;
        }
        else if (fontscale==4){
            webSettings.setDefaultFontSize(25);
            //datetime.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float)28);
            return 4;
        }
        return -1;
    }

    private void displayArticle(){
        // update possible online mode change
        onlineMode = isNetworkAvailable(Objects.requireNonNull(getContext()));

        if (sharedPreferences == null) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        }

        String theStory;
        theStory =  mArticle.getDescription();

        theStory = theStory.replace("=\"//www.", "=\"https://www.")
                .replace("src=\"//", "src=\"https://")
                .replace("=\"/", "=\"https://www.neakriti.gr/")
                .replace("with=\"620\"", "width=\"100%\"");
        String basicWebStory = theStory.replace(Character.toString((char)10), "<br/>");
        String articleDetail   = "<div id='story' class='story'>"+basicWebStory+"</div>";

        String dayNightStyle = "";
        if (isNightMode){
            dayNightStyle =
                    "body,p,div{background:#333333 !Important; color:#eeeeee;} a{color:#ee3333 !Important}" +
                            "blockquote p {background:#444 !Important} "+
                            "blockquote{background:#444 !Important; border-left: 10px solid #666 !Important;} "+
                            "blockquote:before { color:#666 !Important }"+

                            ".descr p {background:#444 !Important} "+
                            ".descr{color: #ccc; background:#444 !Important; border-left: 10px solid #666 !Important;} "+
                            ".descr:before { color:#666 !Important }";
            //binding.articleNestedScrollView.setBackgroundColor(Color.parseColor("#333333"));
        }

        String standardStyle =
                ".video-container{position: relative;padding-bottom: 56.25%;margin-top:20px;height: 0;overflow: hidden;}.video-container iframe, .video-container object, .video-container embed{position: absolute;top: 0;left: 0;width: 100%;height: 100%;}figure{margin-bottom:0;margin-left:0;margin-right:0;padding:0;width=\"100%\"}img{width: 100%;}"+

                        " .descr {background: #eee !Important; color:#444; font-size:0.9em; border-left: 10px solid #ccc; margin: 1.5em 00px; padding: 0.5em 10px;}" +
                        " .descr:before {color: #ccc; line-height: 0.1em; margin-right: 0.25em; vertical-align: -0.4em;}" +

                        " blockquote {background: #eee;border-left: 10px solid #ccc; margin: 1.5em 10px; padding: 0.5em 10px; quotes: '\\201C''\\201D''\\2018''\\2019'}" +
                        " blockquote:before {color: #ccc; content: open-quote; font-size: 4em; line-height: 0.1em; margin-right: 0.25em; vertical-align: -0.4em;}" +
                        " blockquote p {display: inline;} ";

        String webStory =
                "<!DOCTYPE html><html lang=\"el\"><head><title>{0}</title> "+
                        "<meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\"/>"+
                        "<link rel=\"canonical\" href=\"{1}\"> <link href=\"https://fonts.googleapis.com/css?family=Roboto:400,700,900&subset=greek,latin\" rel=\"stylesheet\" type=\"text/css\"> "+
                        "<style> a'{' text-decoration:none; color:#a00; font-weight:600; '}' body'{'line-height:normal; font-family:\"Roboto\"; padding-bottom:50px;{2}'}' "+
                        "object,img,iframe,div,video,param,embed'{'max-width: 100%; '}'{3}{4}</style></head><body>{5}</body></html>";

        String format;
        if (sharedPreferences.getBoolean(getString(R.string.pref_increased_line_distance_key), true)){
            format = "line-height:1.6";
        }
        else{
            format = "";
        }

        webStory = MessageFormat.format(webStory, mArticle.getTitle(), mArticle.getLink(), format, standardStyle, dayNightStyle, articleDetail);
        mWebView.loadDataWithBaseURL(mArticle.getLink(), webStory, "text/html", "utf-8", null);
    }

    /**
     * Depending on user preferences, set the font size for the article
     */
    private void scaleFontSize(){
        if (sharedPreferences == null) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        }
        int scaleFont = sharedPreferences.getInt(getString(R.string.pref_font_size_key), 1);
        switch (scaleFont){
            case 0: webSettings.setDefaultFontSize(15); break;
            case 1: webSettings.setDefaultFontSize(18); break;
            case 2: webSettings.setDefaultFontSize(21); break;
            case 3: webSettings.setDefaultFontSize(23); break;
            case 4: webSettings.setDefaultFontSize(25); break;
        }
        //setZoomButtonsEnabled();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.menu = menu;
        inflater.inflate(R.menu.detail_menu, menu);

        // once the menu is inflated we can modify the "heart" as we enter the article, if its a favorite movie
        if (isArticleInFavorites()){
            menu.findItem(R.id.nav_favorite).setIcon(R.drawable.bookmark_wh_24px);
        }
        else{
            menu.findItem(R.id.nav_favorite).setIcon(R.drawable.bookmark_outline_wh_24px);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_favorite){
            if (isArticleInFavorites()){
                removeArticleFromFavorites();
            }
            else{
                addArticleToFavorites();
            }
        }
        else if (id == R.id.nav_share_article){
            shareArticle();
        }
        else if (id == R.id.nav_tts){
            speak();
        }
        else if (id == R.id.nav_browser){
            // log Firebase Analytics about article sharing
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, getString(R.string.analytics_art_on_broser));
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, getString(R.string.analytics_article_on_broser));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, getString(R.string.analytics_article_on_broser));
            bundle.putString(getString(R.string.analytics_article_title), mArticle.getTitle());
            bundle.putString(getString(R.string.analytics_logged_url), mArticle.getLink());
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Intent browse = new Intent( Intent.ACTION_VIEW , Uri.parse(mArticle.getLink()));
            startActivity( browse );
        }
        else if (id == R.id.nav_settings){
            // <todo>
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

            int scrollviewpadingexists = binding.articleNestedScrollView.getPaddingBottom();
            if (scrollviewpadingexists==0) {
                footerFade(android.R.anim.slide_in_left);
                layoutParams.setMargins(0,0,0, (int)dipToPixels(Objects.requireNonNull(getContext()),90));
                //articleOnBrowserLayout.setLayoutParams(layoutParams);
            }else {
                footerFade(android.R.anim.slide_out_right);
                layoutParams.setMargins(0,0,0,0);
                //articleOnBrowserLayout.setLayoutParams(layoutParams);
            }

        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDestroy(){
        if (mTextToSpeech!=null) {
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
        }
        super.onDestroy();
    }


    private void speak(){
        if (mTextToSpeech==null) {
            Log.d("TTS", "ITS NULL");
            mTextToSpeech = new TextToSpeech(getActivity(), this);
        }
        else {
            Log.d("TTS", "ITS NOT NULL");
        }

        Log.d("Default Engine Info " , mTextToSpeech.getDefaultEngine());

        if (mTextToSpeech.isSpeaking()) {
            mTextToSpeech.stop();
        } else {
            // Check if TTS for the Greek Language - Greece (el_GR) is installed for the TTS Engine Running on the user's device
            int langAvailability = mTextToSpeech.isLanguageAvailable(new Locale("el", "GR"));

            if (langAvailability==TextToSpeech.LANG_AVAILABLE || langAvailability==TextToSpeech.LANG_COUNTRY_AVAILABLE || langAvailability==TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE){
                Log.d("TTS AVAILABILITY", "INSTALLED");
                mTextToSpeech.setLanguage(new Locale("el", "GR"));

                mTextToSpeech.setSpeechRate(1);
                mTextToSpeech.setPitch(1);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mTextToSpeech.speak(AppUtils.makeReadableGreekText(mArticle.getDescription()), TextToSpeech.QUEUE_FLUSH, null, null);
                } else {
                    mTextToSpeech.speak(AppUtils.makeReadableGreekText(mArticle.getDescription()), TextToSpeech.QUEUE_FLUSH, null);
                }
            }
            else if (langAvailability==TextToSpeech.LANG_MISSING_DATA){
                Log.d("TTS AVAILABILITY", "MISSING INSTALLATION");
                AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
                builder
                        .setTitle(R.string.dlg_no_tts_lang_installed_title)
                        .setMessage(R.string.dlg_no_tts_lang_installed_body)
                        .setIcon(R.drawable.warning_48px)
                        .setNegativeButton(R.string.dlg_cancel, (dialog, id) -> {

                        })
                        .setPositiveButton(R.string.dlg_install, (dialog, id) -> {
                            // missing data, install it
                            Intent installTTSIntent = new Intent();
                            installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                            ArrayList<String> languages = new ArrayList<>();
                            languages.add("el-GR");
                            installTTSIntent.putStringArrayListExtra(TextToSpeech.Engine.EXTRA_CHECK_VOICE_DATA_FOR,
                                    languages);
                            startActivity(installTTSIntent);
                        });
                // Create the AlertDialog object and return it
                builder.create().show();
            }
            else if (langAvailability==TextToSpeech.LANG_NOT_SUPPORTED){
                Log.d("TTS AVAILABILITY", "LANGUAGE UNAVAILABLE");

                if (!mTextToSpeech.getDefaultEngine().equals("com.google.android.tts")){
                    AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
                    builder
                            .setTitle(R.string.dlg_no_tts_lang_unavailable_title)
                            .setMessage(R.string.dlg_no_tts_no_google_tts_engine_body)
                            .setIcon(R.drawable.not_interested_48px)
                            .setNegativeButton(R.string.dlg_exit, (dialog, id) -> {

                            });
                    // Create the AlertDialog object and return it
                    builder.create().show();
                }
                else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
                    builder
                            .setTitle(R.string.dlg_no_tts_lang_unavailable_title)
                            .setMessage(R.string.dlg_no_tts_lang_unavailable_body)
                            .setIcon(R.drawable.not_interested_48px)
                            .setNegativeButton(R.string.dlg_exit, (dialog, id) -> {

                            });
                    // Create the AlertDialog object and return it
                    builder.create().show();
                }

                // Setting Text to Speech to null will allow it to detect the changes we did to resolve the failure
                // as making it null mTextToSpeech will re-initialize itself on the next "speak" call
                mTextToSpeech = null;
            }
        }
    }

    /**
     * Article Sharing via implicit intent
     */
    private void shareArticle(){
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("Share")
                .build());

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        String sArticle = "\n"+
                getString(R.string.app_name)+" - "+mArticle.getTitle()+"\n\n"+
                mArticle.getLink() + " \n\n";
        intent.putExtra(Intent.EXTRA_TEXT, sArticle);
        startActivity(Intent.createChooser(intent, getString(R.string.share_the_article)));
    }

    /**
     * Call "insert" on the Content Provider, to insert the existing article to the database
     */
    private void addArticleToFavorites(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(ArticlesDBContract.ArticleEntry.COL_TYPE, ArticlesDBContract.DB_TYPE_FAVORITE);
        contentValues.put(ArticlesDBContract.ArticleEntry.COL_TYPE_ID, "0");

        contentValues.put(ArticlesDBContract.ArticleEntry.COL_GUID, mArticle.getGuid());
        contentValues.put(ArticlesDBContract.ArticleEntry.COL_LINK, mArticle.getLink());
        contentValues.put(ArticlesDBContract.ArticleEntry.COL_TITLE, mArticle.getTitle());
        contentValues.put(ArticlesDBContract.ArticleEntry.COL_DESCRIPTION, mArticle.getDescription());
        contentValues.put(ArticlesDBContract.ArticleEntry.COL_PUB_DATE_STR, mArticle.getPubDateStr());
        contentValues.put(ArticlesDBContract.ArticleEntry.COL_UPDATED_STR, mArticle.getUpdatedStr());
        contentValues.put(ArticlesDBContract.ArticleEntry.COL_PUB_DATE_GRE, mArticle.getPubDateGre());
        contentValues.put(ArticlesDBContract.ArticleEntry.COL_IMG_THUMB, mArticle.getImgThumbStr());
        contentValues.put(ArticlesDBContract.ArticleEntry.COL_IMG_LARGE, mArticle.getImgLargeStr());

        Date pubDate = AppUtils.feedDate(mArticle.getPubDateStr());
        if (pubDate!=null) { contentValues.put(ArticlesDBContract.ArticleEntry.COL_PUB_DATE, pubDate.getTime()); }
        else{ contentValues.put(ArticlesDBContract.ArticleEntry.COL_PUB_DATE, 0); }

        Date updated = AppUtils.feedDate(mArticle.getUpdatedStr());
        if (updated!=null) { contentValues.put(ArticlesDBContract.ArticleEntry.COL_UPDATED, updated.getTime()); }
        else{ contentValues.put(ArticlesDBContract.ArticleEntry.COL_UPDATED, 0); }

        //AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContext().getContentResolver()){};
        //queryHandler.startInsert(1,null,ArticlesDBContract.ArticleEntry.CONTENT_URI, contentValues);

        Uri uri = Objects.requireNonNull(getContext()).getContentResolver().insert(ArticlesDBContract.ArticleEntry.CONTENT_URI, contentValues);
        menu.findItem(R.id.nav_favorite).setIcon(R.drawable.bookmark_wh_24px);
        try {
            Snackbar.make(Objects.requireNonNull(getView()), getString(R.string.snack_bookmark_article_added), Snackbar.LENGTH_SHORT).show();
        }catch (Exception ignored){}
    }

    private Menu menu;

    /**
     * Call "delete" on the Content Provider, to remove the existing article from the database
     */
    private void removeArticleFromFavorites(){
        Uri uri = ArticlesDBContract.ArticleEntry.FAVORITE_CONTENT_URI;
        uri = uri.buildUpon().appendPath(String.valueOf(mArticle.getGuid())).build();
        Objects.requireNonNull(getContext()).getContentResolver().delete(uri, null, null);

        menu.findItem(R.id.nav_favorite).setIcon(R.drawable.bookmark_outline_wh_24px);

        try {
            Snackbar.make(Objects.requireNonNull(getView()), getString(R.string.snack_bookmark_article_removed), Snackbar.LENGTH_SHORT).show();
        }catch (Exception ignored){}
    }

    /**
     * Call "query" on the Content Provider to find out whether or not the article exists in the database
     * @return whether the existing article exists in the favorites database
     */
    private boolean isArticleInFavorites(){
        Cursor cursor = Objects.requireNonNull(getContext()).getContentResolver().query(ArticlesDBContract.ArticleEntry.CONTENT_URI,
                null,
                ArticlesDBContract.ArticleEntry.COL_TYPE + " = " + ArticlesDBContract.DB_TYPE_FAVORITE + " AND " + ArticlesDBContract.ArticleEntry.COL_GUID + " = " + mArticle.getGuid(),
                null,
                null
        );

        assert cursor != null;
        boolean isArticleInFavorites = (cursor.getCount()>0);
        cursor.close();

        return (isArticleInFavorites);
    }

    /**
     * Text to Speech initialization
     * @param status Initialization Status
     */
    @Override
    public void onInit(int status) {
        if (mTextToSpeech==null) {
            mTextToSpeech = new TextToSpeech(getActivity(), this);
        }

        if (status == TextToSpeech.SUCCESS) {
            mTextToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String s) {

                }

                @Override
                public void onDone(String s) {
                    if (s.contains("ok"))
                        mTextToSpeech.shutdown();
                }

                @Override
                public void onError(String s) {

                }
            });
        }
    }

    // <todo>
    void footerFade(int animid){
        Animation myFadeInAnimation = AnimationUtils.loadAnimation(getContext() , animid);
        myFadeInAnimation.setDuration(500);
        footerlayout.startAnimation(myFadeInAnimation);

        if (animid==android.R.anim.slide_out_right){
            footerlayout.postDelayed(() -> {
                binding.articleNestedScrollView.setPadding(0, 0, 0, 0);
                footerlayout.setVisibility(View.GONE);
            }, 500);
        }
        else{
            footerlayout.postDelayed(() -> {
                binding.articleNestedScrollView.setPadding(0, 0, 0, getValueInDIP(90));
                footerlayout.setVisibility(View.VISIBLE);
            }, 500);
        }
    }

    int getValueInDIP(int pxValue){
        Resources res = getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, pxValue, res.getDisplayMetrics());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mTextToSpeech!=null) {
            if (mTextToSpeech.isSpeaking()) {
                mTextToSpeech.stop();
            }
            mTextToSpeech.shutdown();
        }
    }

    /**
     * Load the facebook comments for the displayed article
     * Source: https://www.androidhive.info/2016/09/android-adding-facebook-comments-widget-in-app/
     */
    private void loadComments() {
        mWebViewComments.setWebViewClient(new UriWebViewClient());
        mWebViewComments.setWebChromeClient(new UriChromeClient());
        mWebViewComments.getSettings().setJavaScriptEnabled(true);
        mWebViewComments.getSettings().setAppCacheEnabled(true);
        mWebViewComments.getSettings().setDomStorageEnabled(true);
        mWebViewComments.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebViewComments.getSettings().setSupportMultipleWindows(true);
        mWebViewComments.getSettings().setSupportZoom(false);
        mWebViewComments.getSettings().setBuiltInZoomControls(false);
        CookieManager.getInstance().setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= 21) {
            mWebViewComments.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager.getInstance().setAcceptThirdPartyCookies(mWebViewComments, true);
        }

        // facebook comment widget including the article url
        String facebookComments =
                " <!doctype html> <html lang='el'> <head></head> <body> " +
                        " <div id='fb-root'></div>" +
                        " <script>(function(d, s, id) {" +
                        "  var js, fjs = d.getElementsByTagName(s)[0];" +
                        "  if (d.getElementById(id)) return;" +
                        "  js = d.createElement(s); js.id = id;" +
                        "  js.src = 'https://connect.facebook.net/el_GR/sdk.js#xfbml=1&version=v3.1&appId=56856387271&autoLogAppEvents=1';" +
                        "  fjs.parentNode.insertBefore(js, fjs);" +
                        "}(document, 'script', 'facebook-jssdk'));</script>" +

                        " <div class='fb-comments' data-href='" + mArticle.getLink() + "' data-num-posts='30'></div> " +
                        " </body> </html>";


        mWebViewComments.loadDataWithBaseURL("https://www.neakriti.gr", facebookComments, "text/html", "UTF-8", null);
        mWebViewComments.setMinimumHeight(200);
    }


    private class UriWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            String host = Uri.parse(url).getHost();
            assert host != null;
            return !host.equals("m.facebook.com");
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            String host = Uri.parse(url).getHost();
            if (url.contains("/plugins/close_popup.php?reload")) {
                final Handler handler = new Handler();
                handler.postDelayed(() -> {
                    //Do something after 100ms
                    mContainer.removeView(mWebviewPop);
                    loadComments();
                }, 600);
            }
        }
    }

    class UriChromeClient extends WebChromeClient {

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog,
                                      boolean isUserGesture, Message resultMsg) {
            mWebviewPop = new WebView(getContext());
            mWebviewPop.setVerticalScrollBarEnabled(false);
            mWebviewPop.setHorizontalScrollBarEnabled(false);
            mWebviewPop.setWebViewClient(new UriWebViewClient());
            mWebviewPop.setWebChromeClient(this);
            mWebviewPop.getSettings().setJavaScriptEnabled(true);
            mWebviewPop.getSettings().setDomStorageEnabled(true);
            mWebviewPop.getSettings().setSupportZoom(false);
            mWebviewPop.getSettings().setBuiltInZoomControls(false);
            mWebviewPop.getSettings().setSupportMultipleWindows(true);
            mWebviewPop.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            mContainer.addView(mWebviewPop);
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(mWebviewPop);
            resultMsg.sendToTarget();

            return true;
        }

        @Override
        public void onCloseWindow(WebView window) {
        }
    }
}

package eu.anifantakis.neakriti;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import eu.anifantakis.neakriti.data.db.ArticlesDBContract;
import eu.anifantakis.neakriti.data.feed.gson.Article;
import eu.anifantakis.neakriti.databinding.FragmentArticleDetailBinding;
import eu.anifantakis.neakriti.utils.AppUtils;
import eu.anifantakis.neakriti.utils.NeaKritiApp;

import static eu.anifantakis.neakriti.utils.AppUtils.isNightMode;
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
    private CollapsingToolbarLayout appBarLayout;
    private String mCategoryTitle;

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

        mTracker = ((NeaKritiApp) getActivity().getApplication()).getDefaultTracker();
        mTextToSpeech = new TextToSpeech(getActivity(), this);

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
        outState.putParcelable(AppUtils.EXTRAS_ARTICLE, mArticle);
        super.onSaveInstanceState(outState);
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

        mWebView = binding.articleDetail;
        webSettings = mWebView.getSettings();

        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        mWebView.getSettings().setAllowFileAccess(true);
        webSettings.setAppCacheEnabled(true);

        // set the webview scrollbar to be "inside" to avoid unecessary "right-side" padding space
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        adView = binding.adView;

        // Handling Rotation
        if (savedInstanceState!=null){
            if (savedInstanceState.containsKey(AppUtils.EXTRAS_ARTICLE)) {
                mArticle = savedInstanceState.getParcelable(AppUtils.EXTRAS_ARTICLE);
            }
        }
        Activity activity = this.getActivity();
        appBarLayout = activity.findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(mArticle.getGroupName());
        }

        // Show the dummy content as text in a TextView.
        if (mArticle != null) {
            scaleFontSize();
            displayArticle();
        }

        return rootView;
    }

    private void displayArticle(){
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
            dayNightStyle = "body,p,div{background:#333333 !Important; color:#eeeeee;} a{color:#ee3333 !Important}";
            //binding.articleNestedScrollView.setBackgroundColor(Color.parseColor("#333333"));
        }

        String webStory =
                "<!DOCTYPE html><html lang=\"el\"><head><title>{0}</title> "+
                "<meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\"/>"+
                "<link rel=\"canonical\" href=\"{1}\"> <link href=\"https://fonts.googleapis.com/css?family=Roboto:400,700,900&subset=greek,latin\" rel=\"stylesheet\" type=\"text/css\"> "+
                "<style> a'{' text-decoration:none; color:#a00; font-weight:600; '}' body'{'line-height:normal; font-family:\"Roboto\"; padding-bottom:50px;{2}'}' "+
                "object,img,iframe,div,video,param,embed'{'max-width: 100%; '}'{3}</style></head><body>{4}</body></html>";

        String format;
        if (sharedPreferences.getBoolean(getString(R.string.pref_increased_line_distance_key), true)){
            format = "line-height:1.6";
        }
        else{
            format = "";
        }

        webStory = MessageFormat.format(webStory, mArticle.getTitle(), mArticle.getLink(), format, dayNightStyle, articleDetail);
        mWebView.loadDataWithBaseURL(null, webStory, "text/html", "utf-8", null);

        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);
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

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        if (mTextToSpeech!=null) {
            mTextToSpeech.shutdown();
        }

    }


    private void speak(){
        if (mTextToSpeech==null)
            mTextToSpeech = new TextToSpeech(getActivity(), this);

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
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder
                        .setTitle(R.string.dlg_no_tts_lang_installed_title)
                        .setMessage(R.string.dlg_no_tts_lang_installed_body)
                        .setIcon(R.drawable.warning_48px)
                        .setNegativeButton(R.string.dlg_cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        })
                        .setPositiveButton(R.string.dlg_install, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // missing data, install it
                                Intent installTTSIntent = new Intent();
                                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                                ArrayList<String> languages = new ArrayList<>();
                                languages.add("el-GR");
                                installTTSIntent.putStringArrayListExtra(TextToSpeech.Engine.EXTRA_CHECK_VOICE_DATA_FOR,
                                        languages);
                                startActivity(installTTSIntent);
                            }
                        });
                // Create the AlertDialog object and return it
                builder.create().show();
            }
            else if (langAvailability==TextToSpeech.LANG_NOT_SUPPORTED){
                Log.d("TTS AVAILABILITY", "LANGUAGE UNAVAILABLE");

                if (!mTextToSpeech.getDefaultEngine().equals("com.google.android.tts")){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder
                            .setTitle(R.string.dlg_no_tts_lang_unavailable_title)
                            .setMessage(R.string.dlg_no_tts_no_google_tts_engine_body)
                            .setIcon(R.drawable.not_interested_48px)
                            .setNegativeButton(R.string.dlg_exit, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                }
                            });
                    // Create the AlertDialog object and return it
                    builder.create().show();
                }
                else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder
                        .setTitle(R.string.dlg_no_tts_lang_unavailable_title)
                        .setMessage(R.string.dlg_no_tts_lang_unavailable_body)
                        .setIcon(R.drawable.not_interested_48px)
                        .setNegativeButton(R.string.dlg_exit, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
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

        Date updated = AppUtils.feedDateUpdated(mArticle.getUpdatedStr());
        if (updated!=null) { contentValues.put(ArticlesDBContract.ArticleEntry.COL_UPDATED, updated.getTime()); }
        else{ contentValues.put(ArticlesDBContract.ArticleEntry.COL_UPDATED, 0); }

        //AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContext().getContentResolver()){};
        //queryHandler.startInsert(1,null,ArticlesDBContract.ArticleEntry.CONTENT_URI, contentValues);

        Uri uri = getContext().getContentResolver().insert(ArticlesDBContract.ArticleEntry.CONTENT_URI, contentValues);
        menu.findItem(R.id.nav_favorite).setIcon(R.drawable.bookmark_wh_24px);
        Snackbar.make(getView(), getString(R.string.snack_bookmark_article_added), Snackbar.LENGTH_SHORT).show();
    }

    private Menu menu;

    /**
     * Call "delete" on the Content Provider, to remove the existing article from the database
     */
    private void removeArticleFromFavorites(){
        Uri uri = ArticlesDBContract.ArticleEntry.FAVORITE_CONTENT_URI;
        uri = uri.buildUpon().appendPath(String.valueOf(mArticle.getGuid())).build();
        getContext().getContentResolver().delete(uri, null, null);

        menu.findItem(R.id.nav_favorite).setIcon(R.drawable.bookmark_outline_wh_24px);

        Snackbar.make(getView(), getString(R.string.snack_bookmark_article_removed), Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Call "query" on the Content Provider to find out whether or not the article exists in the database
     * @return whether the existing article exists in the favorites database
     */
    private boolean isArticleInFavorites(){
        Cursor cursor = getContext().getContentResolver().query(ArticlesDBContract.ArticleEntry.CONTENT_URI,
                null,
                ArticlesDBContract.ArticleEntry.COL_TYPE + " = " + ArticlesDBContract.DB_TYPE_FAVORITE + " AND " + ArticlesDBContract.ArticleEntry.COL_GUID + " = " + mArticle.getGuid(),
                null,
                null
        );

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
        if (mTextToSpeech==null)
            mTextToSpeech = new TextToSpeech(getActivity(), this);

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
}

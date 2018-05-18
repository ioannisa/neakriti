package eu.anifantakis.neakriti;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;

import java.io.IOException;
import java.util.Date;

import eu.anifantakis.neakriti.data.ArticlesListAdapter;
import eu.anifantakis.neakriti.data.RequestInterface;
import eu.anifantakis.neakriti.data.db.ArticlesDBContract;
import eu.anifantakis.neakriti.data.feed.Article;
import eu.anifantakis.neakriti.data.feed.ArticlesCollection;
import eu.anifantakis.neakriti.data.feed.RssFeed;
import eu.anifantakis.neakriti.databinding.ActivityArticleListBinding;
import eu.anifantakis.neakriti.utils.AppUtils;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

import static eu.anifantakis.neakriti.utils.AppUtils.sRadioPlayer;


public class ArticleListActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        ArticlesListAdapter.ArticleItemClickListener,
        LoaderManager.LoaderCallbacks<ArticlesCollection>,
        SwipeRefreshLayout.OnRefreshListener,
        Player.EventListener{

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private ActivityArticleListBinding binding;
    private ArticlesListAdapter mArticlesListAdapter;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Retrofit retrofit;
    private ViewGroup liveView;
    private static ArticlesCollection cachedCollection = null;
    private ImageView btnRadio;
    private  boolean clickedAtLeastOneItem = false;
    private static boolean exoPlayerIsPlaying = false;
    private TextView feedCategoryTitle;
    private ItemTouchHelper itemTouchHelper;

    private static final int ARTICLES_FEED_LOADER = 0;
    private static final String LOADER_TYPE = "LOADER_TYPE";
    private static final String LOADER_ID = "LOADER_ID";
    private static final String LOADER_ITEMS_COUNT = "LOADER_ITEMS_COUNT";
    private static final String CACHED_COLLECTION = "CACHED_COLLECTION";
    private static final String LIVE_PANEL_VISIBILITY = "LIVE_PANEL_VISIBILITY";
    private static final String STATE_EXO_PLAYER_RADIO_PLAYING = "exo_player_radio_playing";
    private static final String STATE_CLICKED_AN_ITEM = "STATE_CLICKED_AN_ITEM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_article_list);
        //setContentView(R.layout.activity_article_list);

        FirebaseApp.initializeApp(this);
        FirebaseMessaging.getInstance().subscribeToTopic("neakriti-android-news-test");

        Toolbar toolbar = binding.masterView.toolbar; //(Toolbar) findViewById(R.id.toolbar); // binding.masterView.toolbar;
        setSupportActionBar(toolbar);
        //toolbar.setTitle(getTitle());

        feedCategoryTitle = binding.masterView.articles.incLivePanel.feedCategoryTitle;
        btnRadio = binding.masterView.articles.incLivePanel.btnLiveRadio;
        initializeRadioExoPlayer();

        liveView = binding.masterView.articles.incLivePanel.liveView;

        if (savedInstanceState!=null){
            cachedCollection = savedInstanceState.getParcelable(CACHED_COLLECTION);
            liveView.setVisibility(savedInstanceState.getInt(LIVE_PANEL_VISIBILITY));

            if (savedInstanceState.containsKey(STATE_EXO_PLAYER_RADIO_PLAYING)) {
                exoPlayerIsPlaying = savedInstanceState.getBoolean(STATE_EXO_PLAYER_RADIO_PLAYING);
            }
            if (exoPlayerIsPlaying){
                btnRadio.setImageResource(R.drawable.btn_radio_pause);
            }
            if (savedInstanceState.containsKey(STATE_CLICKED_AN_ITEM)) {
                clickedAtLeastOneItem = savedInstanceState.getBoolean(STATE_CLICKED_AN_ITEM);
            }
        }
        else{
            liveView.setVisibility(View.GONE);
        }

        FloatingActionButton fab = binding.masterView.fab;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //if (findViewById(R.id.article_detail_container) != null) {
        if (binding.masterView.articles.articleDetailContainer != null){
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        if (mTwoPane && !clickedAtLeastOneItem){
            binding.masterView.articles.articleDetailContainer.setVisibility(View.GONE);
        }

        DrawerLayout drawer = binding.drawerLayout;// (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = binding.navView;// (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mSwipeRefreshLayout = binding.masterView.articles.mainLayoutSwipe; //findViewById(R.id.main_layout_swipe);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        // SETUP RETROFIT

        retrofit = new Retrofit.Builder()
                .baseUrl(AppUtils.BASE_URL)
                .client(new OkHttpClient())
                .addConverterFactory(SimpleXmlConverterFactory.createNonStrict(new Persister(new AnnotationStrategy())))
                .build();

        // SETUP RECYCLER VIEW
        mRecyclerView = binding.masterView.articles.articleList; //findViewById(R.id.article_list); //binding.masterView.articles.articleList;//
        //assert mRecyclerView != null;

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        //mRecyclerView.setHasFixedSize(true);
        mArticlesListAdapter = new ArticlesListAdapter(this);
        mRecyclerView.setAdapter(mArticlesListAdapter);

        itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int id = (int) viewHolder.itemView.getTag();

                Uri uri = ArticlesDBContract.ArticleEntry.CONTENT_URI;
                uri = uri.buildUpon().appendPath(String.valueOf(id)).build();
                getContentResolver().delete(uri, null, null);

                cachedCollection = null;
                makeArticlesLoaderQuery(ArticlesDBContract.DB_TYPE_FAVORITE, "0", 200);
            }
        });

        feedSrvid = "127";
        feedItems = 25;
        feedName = getString(R.string.nav_home);
        makeArticlesLoaderQuery(ArticlesDBContract.DB_TYPE_CATEGORY, feedSrvid, feedItems);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // we don't want the selection to remain visible via article row color when we are on phone
        // when we come back from the detail activity.  This is useful only on tablets
        if (!mTwoPane) {
            mArticlesListAdapter.clearRowSelection();
        }
    }

    /**
     * Interface implementation (defined in the adapter) for the clicking of an item in the articles recycler view
     * @param clickedItemIndex
     */
    @Override
    public void onArticleItemClick(int clickedItemIndex, ImageView sharedImage) {
        Log.d("RV ACTION", "ITEM CLICK");

        String title = mArticlesListAdapter.getArticleAtIndex(clickedItemIndex).getTitle();
        Article article = mArticlesListAdapter.getArticleAtIndex(clickedItemIndex);

        // display in the Two Pane version (tablet) the middle area where the article will be displayed
        if (mTwoPane && !clickedAtLeastOneItem) {
            binding.masterView.articles.articleDetailContainer.setVisibility(View.VISIBLE);
            clickedAtLeastOneItem = true;
        }

        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(AppUtils.EXTRAS_ARTICLE, article);
            ArticleDetailFragment fragment = new ArticleDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.article_detail_container, fragment)
                    .commit();
        } else {
            Intent intent = new Intent(this, ArticleDetailActivity.class);
            article.setGroupName(feedName);
            intent.putExtra(AppUtils.EXTRAS_ARTICLE, article);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                Log.d("TRANSITION NAME", ViewCompat.getTransitionName(sharedImage));
            }

            //Bitmap bm=((BitmapDrawable)sharedImage.getDrawable()).getBitmap();
            //intent.putExtra(AppUtils.EXTRAS_LOW_RES_BITMAP, bm);

            // bundle for the transition effect
            Bundle bundle = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bundle = ActivityOptionsCompat
                        .makeSceneTransitionAnimation(
                                this,
                                sharedImage,
                                ViewCompat.getTransitionName(sharedImage) //sharedImage.getTransitionName()
                        ).toBundle();

                if (feedType == ArticlesDBContract.DB_TYPE_FAVORITE){
                    ActivityCompat.startActivityForResult(this, intent, 1, bundle);
                }
                else {
                    startActivity(intent, bundle);
                }
            }
            else{
                if (feedType == ArticlesDBContract.DB_TYPE_FAVORITE) {
                    startActivityForResult(intent, 1);
                }
                else {
                    startActivity(intent);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if we are back from another activity and the type is favorites, refresh because we might
        // have removed an item from the favorite list
        if (feedType == ArticlesDBContract.DB_TYPE_FAVORITE){
            cachedCollection = null;
            makeArticlesLoaderQuery(ArticlesDBContract.DB_TYPE_FAVORITE, "0", feedItems);
        }
    }

    private void makeArticlesLoaderQuery(int type, String id, int items){
        Bundle bundle = new Bundle();
        bundle.putInt(LOADER_TYPE, type);
        bundle.putString(LOADER_ID, id);
        bundle.putInt(LOADER_ITEMS_COUNT, items);

        feedCategoryTitle.setText(feedName);

        Loader<RssFeed> loader = getSupportLoaderManager().getLoader(ARTICLES_FEED_LOADER);
        if (loader == null) {
            getSupportLoaderManager().initLoader(ARTICLES_FEED_LOADER, bundle, this);
        } else {
            getSupportLoaderManager().restartLoader(ARTICLES_FEED_LOADER, bundle, this);
        }
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<ArticlesCollection> onCreateLoader(int i, final Bundle bundle) {
        Log.d("LOADING", "ON CREATE LOADER");
        return new AsyncTaskLoader<ArticlesCollection>(this) {
            @Override
            public ArticlesCollection loadInBackground() {
                RssFeed feed = null;
                int fetchType = bundle.getInt(LOADER_TYPE);

                if (fetchType == ArticlesDBContract.DB_TYPE_FAVORITE){
                    Cursor cursor = getContentResolver().query(ArticlesDBContract.ArticleEntry.CONTENT_URI,
                            null,
                            ArticlesDBContract.ArticleEntry.COL_TYPE + " = " + ArticlesDBContract.DB_TYPE_FAVORITE,
                            null,
                            ArticlesDBContract.ArticleEntry._ID + " DESC"
                    );

                    if (cursor!=null) {
                        if (cursor.getCount() > 0) {
                            ArticlesCollection favoriteArticles = new ArticlesCollection();

                            cursor.moveToFirst();
                            while (!cursor.isAfterLast()){
                                Article article = new Article();

                                article.setGuid(cursor.getInt(cursor.getColumnIndex(ArticlesDBContract.ArticleEntry.COL_GUID)));
                                article.setLink(cursor.getString(cursor.getColumnIndex(ArticlesDBContract.ArticleEntry.COL_LINK)));
                                article.setTitle(cursor.getString(cursor.getColumnIndex(ArticlesDBContract.ArticleEntry.COL_TITLE)));
                                article.setDescription(cursor.getString(cursor.getColumnIndex(ArticlesDBContract.ArticleEntry.COL_DESCRIPTION)));
                                article.setPubDateStr(cursor.getString(cursor.getColumnIndex(ArticlesDBContract.ArticleEntry.COL_PUB_DATE_STR)));
                                article.setUpdatedStr(cursor.getString(cursor.getColumnIndex(ArticlesDBContract.ArticleEntry.COL_UPDATED_STR)));
                                article.setPubDateGre(cursor.getString(cursor.getColumnIndex(ArticlesDBContract.ArticleEntry.COL_PUB_DATE_GRE)));
                                article.setImgThumb(cursor.getString(cursor.getColumnIndex(ArticlesDBContract.ArticleEntry.COL_IMG_THUMB)));
                                article.setImgLarge(cursor.getString(cursor.getColumnIndex(ArticlesDBContract.ArticleEntry.COL_IMG_LARGE)));

                                favoriteArticles.addArticle(article);
                                cursor.moveToNext();
                            }
                            // all results of the favorites are contained in a single page (no endless load here)
                            return favoriteArticles;
                        }
                    }
                }
                else {
                    try {
                        RequestInterface request = retrofit.create(RequestInterface.class);
                        Call<RssFeed> call = request.getFeedByCategory(bundle.getString(LOADER_ID), bundle.getInt(LOADER_ITEMS_COUNT));
                        // make a synchronous retrofit call in our async task loader
                        feed = call.execute().body();

                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }

                    return new ArticlesCollection(feed.getChannel().getItemList());
                }
                return null;
            }


            @Override
            protected void onStartLoading() {
                if (cachedCollection == null) {
                    Log.d("LOADER", "FETCHING NEW DATA");
                    mSwipeRefreshLayout.setRefreshing(true);
                    forceLoad();
                } else {
                    Log.d("LOADER", "SHOWING CACHED DATA");
                    deliverResult(cachedCollection);
                }
            }

            @Override
            public void deliverResult(ArticlesCollection data) {
                cachedCollection = data;
                mArticlesListAdapter.setCollection(data);
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<ArticlesCollection> loader, ArticlesCollection data) {
        Log.d("LOADING", "LOAD FINISHED");

        mSwipeRefreshLayout.setRefreshing(false);
    }


    @Override
    public void onLoaderReset(Loader<ArticlesCollection> loader) {

    }


    //============================ APP DRAWER =====================================

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = binding.drawerLayout;// (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_live){
            switchLivePanelVisibility();
        }

        //noinspection SimplifiableIfStatement
        else if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Changes the livePanel's visibility from visible to gone with an animation
     */
    private void switchLivePanelVisibility(){
        liveView.clearAnimation();
        liveView.invalidate();

        if (liveView.getVisibility()==View.GONE){
            liveView.setVisibility(View.VISIBLE);
            AppUtils.setLayoutAnim_slidedown(liveView, false);
        }
        else{
            liveView.setVisibility(View.GONE);
            AppUtils.setLayoutAnim_slideup(liveView, true);
        }
    }

    private static int feedType = -1;
    private static String feedName = "";
    private static String feedSrvid = "";
    private static int feedItems = 0;

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        cachedCollection = null;
        itemTouchHelper.attachToRecyclerView(null);

        if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        else if (id == R.id.nav_favorite_items){
            feedType = ArticlesDBContract.DB_TYPE_FAVORITE;
            feedItems = 200;
            feedName = item.getTitle().toString();
            makeArticlesLoaderQuery(ArticlesDBContract.DB_TYPE_FAVORITE, "0", feedItems);

            itemTouchHelper.attachToRecyclerView(mRecyclerView);
        }
        else {
            feedType = ArticlesDBContract.DB_TYPE_CATEGORY;
            feedItems = 25;
            feedName = item.getTitle().toString();

            if (id == R.id.nav_home)            { feedSrvid = "127"; }
            else if (id == R.id.nav_crete)      { feedSrvid = "95"; }
            else if (id == R.id.nav_views)      { feedSrvid = "322"; }
            else if (id == R.id.nav_economy)    { feedSrvid = "159"; }
            else if (id == R.id.nav_culture)    { feedSrvid = "116"; }
            else if (id == R.id.nav_pioneering) { feedSrvid = "131"; }
            else if (id == R.id.nav_sports)     { feedSrvid = "225"; }
            else if (id == R.id.nav_lifestyle)  { feedSrvid = "133"; }
            else if (id == R.id.nav_health)     { feedSrvid = "115"; }
            else if (id == R.id.nav_woman)      { feedSrvid = "128"; }
            else if (id == R.id.nav_travel)     { feedSrvid = "263"; }

            makeArticlesLoaderQuery(ArticlesDBContract.DB_TYPE_CATEGORY, feedSrvid, feedItems);
        }

        DrawerLayout drawer = binding.drawerLayout;// (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRefresh() {
        cachedCollection = null;
        makeArticlesLoaderQuery(feedType, feedSrvid, feedItems);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(CACHED_COLLECTION, cachedCollection);
        outState.putInt(LIVE_PANEL_VISIBILITY, liveView.getVisibility());
        outState.putBoolean(STATE_EXO_PLAYER_RADIO_PLAYING, exoPlayerIsPlaying);
        outState.putBoolean(STATE_CLICKED_AN_ITEM, clickedAtLeastOneItem);
    }

    public void actionUpClicked(View view) {
        super.onBackPressed();
    }

    public void liveStreamTVClicked(View view){
        Intent intent = new Intent(this, TVStreamActivity.class);
        startActivity(intent);
    }

    public void liveStreamRadioClicked(View view){
        sRadioPlayer.setPlayWhenReady(!exoPlayerIsPlaying);
        if (exoPlayerIsPlaying){
            Picasso.with(this)
                    .load(R.drawable.btn_radio_pause)
                    .into(btnRadio);
        }
        else{
            Picasso.with(this)
                    .load(R.drawable.btn_radio)
                    .into(btnRadio);
        }
    }

    private void initializeRadioExoPlayer(){
        if (sRadioPlayer == null){
            TrackSelector trackSelector = new DefaultTrackSelector(
                    new AdaptiveTrackSelection.Factory(
                            new DefaultBandwidthMeter()
                    )
            );

            sRadioPlayer = ExoPlayerFactory.newSimpleInstance(
                    getApplicationContext(),
                    trackSelector
            );
            sRadioPlayer.addListener(this);

            String userAgent = Util.getUserAgent(getApplicationContext(), "rssreadernk");

            MediaSource source = new ExtractorMediaSource(
                    Uri.parse("http://eco.onestreaming.com:8237/live"),
                    new OkHttpDataSourceFactory(
                            new OkHttpClient(),
                            userAgent,
                            null
                    ),
                    new DefaultExtractorsFactory(),
                    null,
                    null
            );

            sRadioPlayer.prepare(source);
            //sRadioPlayer.setPlayWhenReady(true);

        }
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if((playbackState == Player.STATE_READY) && playWhenReady){
            Log.d("RssReader", "onPlayerStateChanged: PLAYING");
            exoPlayerIsPlaying=true;
            //makeNotification();
        } else if((playbackState == Player.STATE_READY)){
            Log.d("RssReader", "onPlayerStateChanged: PAUSED");
            exoPlayerIsPlaying=false;
            //if (mNotificationManager!=null)
            //    mNotificationManager.cancel(NOTIFICATION_RADIO984_ID);
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }
}

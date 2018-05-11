package eu.anifantakis.neakriti;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Intent;
import android.content.Loader;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;

import java.io.IOException;
import java.util.List;

import eu.anifantakis.neakriti.data.ArticlesListAdapter;
import eu.anifantakis.neakriti.data.RequestInterface;
import eu.anifantakis.neakriti.data.model.Article;
import eu.anifantakis.neakriti.data.model.ArticlesCollection;
import eu.anifantakis.neakriti.data.model.RssFeed;
import eu.anifantakis.neakriti.databinding.ActivityArticleListBinding;
import eu.anifantakis.neakriti.utils.AppUtils;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;


public class ArticleListActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        ArticlesListAdapter.ArticleItemClickListener,
        LoaderManager.LoaderCallbacks<ArticlesCollection>,
        SwipeRefreshLayout.OnRefreshListener {

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
    private static ArticlesCollection cachedCollection = null;

    private static final int ARTICLES_FEED_LOADER = 0;
    private static final String LOADER_SRVID = "LOADER_SRVID";
    private static final String LOADER_ITEMS_COUNT = "LOADER_ITEMS_COUNT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_article_list);
        //setContentView(R.layout.activity_article_list);

        Toolbar toolbar = binding.masterView.toolbar; //(Toolbar) findViewById(R.id.toolbar); // binding.masterView.toolbar;
        setSupportActionBar(toolbar);
        //toolbar.setTitle(getTitle());

        if (savedInstanceState!=null){
            cachedCollection = savedInstanceState.getParcelable("xx");
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

        mRecyclerView.setHasFixedSize(true);
        mArticlesListAdapter = new ArticlesListAdapter(this);
        mRecyclerView.setAdapter(mArticlesListAdapter);

        feedSrvid = "127";
        feedItems = 25;
        makeArticlesLoaderQuery(feedSrvid, feedItems);
    }

    /**
     * Interface implementation (defined in the adapter) for the clicking of an item in the articles recycler view
     * @param clickedItemIndex
     */
    @Override
    public void onArticleItemClick(int clickedItemIndex) {
        Log.d("RV ACTION", "ITEM CLICK");

        String title = mArticlesListAdapter.getArticleAtIndex(clickedItemIndex).getTitle();
        Article article = mArticlesListAdapter.getArticleAtIndex(clickedItemIndex);

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
            intent.putExtra(AppUtils.EXTRAS_ARTICLE, article);
            startActivity(intent);
        }
    }


    private void makeArticlesLoaderQuery(String srvid, int items){

        Bundle bundle = new Bundle();
        bundle.putString(LOADER_SRVID, srvid);
        bundle.putInt(LOADER_ITEMS_COUNT, items);

        Loader<RssFeed> loader = getLoaderManager().getLoader(ARTICLES_FEED_LOADER);
        if (loader == null) {
            getLoaderManager().initLoader(ARTICLES_FEED_LOADER, bundle, this);
        } else {
            getLoaderManager().restartLoader(ARTICLES_FEED_LOADER, bundle, this);
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
                try {
                    RequestInterface request = retrofit.create(RequestInterface.class);
                    Call<RssFeed> call = request.getFeedByCategory(bundle.getString(LOADER_SRVID), bundle.getInt(LOADER_ITEMS_COUNT));
                    // make a synchronous retrofit call in our async task loader
                    feed = call.execute().body();

                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }

                return new ArticlesCollection(feed.getChannel().getItemList());
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
    public void onLoadFinished(Loader<ArticlesCollection> loader, ArticlesCollection articlesCollection) {
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private static String feedSrvid = "";
    private static int feedItems = 0;

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        cachedCollection = null;

        if (id == R.id.nav_camera) {
            feedSrvid = "127";
            feedItems = 25;
        } else if (id == R.id.nav_gallery) {
            feedSrvid = "159";
            feedItems = 25;
        } else if (id == R.id.nav_slideshow) {
            feedSrvid = "225";
            feedItems = 25;
        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        makeArticlesLoaderQuery(feedSrvid, feedItems);

        DrawerLayout drawer = binding.drawerLayout;// (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRefresh() {
        cachedCollection = null;
        makeArticlesLoaderQuery(feedSrvid, feedItems);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("xx", cachedCollection);
    }
}

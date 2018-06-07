package eu.anifantakis.neakriti;

import android.content.Intent;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import eu.anifantakis.neakriti.data.feed.gson.Article;
import eu.anifantakis.neakriti.databinding.ActivityArticleDetailBinding;
import eu.anifantakis.neakriti.utils.AppUtils;

import static eu.anifantakis.neakriti.utils.AppUtils.isNightMode;

public class ArticleDetailActivity extends AppCompatActivity {

    protected ActivityArticleDetailBinding binding;
    private boolean startedByNotification = false;

    private Article mArticle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_article_detail);
        //setContentView(R.layout.activity_article_detail);

        supportPostponeEnterTransition();
        ImageView detailActivityImage = binding.detailActivityImage;

        // Receive the Parcelable Movie object from the extras of the intent.
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(AppUtils.EXTRAS_CUSTOM_CATEGORY_TITLE)) {
                setTitle(getIntent().getStringExtra(AppUtils.EXTRAS_CUSTOM_CATEGORY_TITLE));
            }

            if (extras.containsKey(AppUtils.EXTRAS_ARTICLE)) {
                mArticle = getIntent().getParcelableExtra(AppUtils.EXTRAS_ARTICLE);
            }

            if (extras.containsKey(AppUtils.EXTRAS_ORIGIN_NOTIFICATION)){
                startedByNotification = getIntent().getBooleanExtra(AppUtils.EXTRAS_ORIGIN_NOTIFICATION, false);
            }

            // place directly the low res image from the main activity so there are no delays in the Transition
            // then later we will load the higher res image
            if (extras.containsKey(AppUtils.EXTRAS_LOW_RES_BITMAP)){
                Bitmap lowResBitmap = getIntent().getParcelableExtra(AppUtils.EXTRAS_LOW_RES_BITMAP);
                if (lowResBitmap !=null)
                    detailActivityImage.setImageBitmap(lowResBitmap);

                supportStartPostponedEnterTransition();
            }
            else if (startedByNotification){
                // we don't care about transition - continue to activity without waiting for image load
                supportStartPostponedEnterTransition();

                Picasso.get()
                        .load(mArticle.getImgThumbStr())
                        .noFade()
                        .into(detailActivityImage);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            detailActivityImage.setTransitionName(Integer.toString(mArticle.getGuid()));
            Log.d("TRANSITION RECEIVED", Integer.toString(mArticle.getGuid()));
        }

        if (mArticle.getImgThumb()==null) {
            binding.toolbarLayout.setVisibility(View.GONE);
            supportStartPostponedEnterTransition();
        }

        setSupportActionBar(binding.detailToolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();

            arguments.putParcelable(AppUtils.EXTRAS_ARTICLE, mArticle);

            ArticleDetailFragment fragment = new ArticleDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.article_detail_container, fragment)
                    .commit();
        }
    }

    /**
     * IF WE MAKE USE OF THE ACTUAL ANDROID TOOLBAR...
     * When the arrow is pressed on the action bar, close the activity
     * by invoking the original "back button pressing". This is to reverse transition
     * @param item The menu item that is responsible for the back action
     * @return true if that item was clicked
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                if (startedByNotification) {
                    startMainActivity();
                }
                else {
                    supportFinishAfterTransition();
                }
                super.onBackPressed();
                return true;
            }
        }
        /*
        if (id == android.R.id.home) {
            NavUtils.navigateUpTo(this, new Intent(this, ArticleListActivity.class));
            return true;
        }*/
        return super.onOptionsItemSelected(item);
    }

    private void startMainActivity(){
        Intent intent = new Intent(getApplicationContext(), ArticleListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        //NavUtils.navigateUpTo(this, new Intent(this, ArticleListActivity.class));
        if (startedByNotification) {
            startMainActivity();
        }
        ArticleListActivity.shouldreload = startedByNotification;
        super.onBackPressed();
    }

    @Override
    public Resources.Theme getTheme() {
        Resources.Theme theme = super.getTheme();
        if(isNightMode){
            theme.applyStyle(R.style.NoActionBarNight, true);
        }
        else{
            theme.applyStyle(R.style.NoActionBar, true);
        }
        return super.getTheme();
    }
}

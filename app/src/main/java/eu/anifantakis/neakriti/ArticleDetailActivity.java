package eu.anifantakis.neakriti;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import eu.anifantakis.neakriti.data.feed.Article;
import eu.anifantakis.neakriti.utils.AppUtils;

public class ArticleDetailActivity extends AppCompatActivity {

    private boolean startedByNotification = false;

    private Article mArticle;
    private ImageView detailActivityImage;
    private Bitmap lowResBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        supportPostponeEnterTransition();
        detailActivityImage = (ImageView) findViewById(R.id.detail_activity_image);

        // Receive the Parcelable Movie object from the extras of the intent.
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(AppUtils.EXTRAS_ARTICLE)) {
                mArticle = getIntent().getParcelableExtra(AppUtils.EXTRAS_ARTICLE);
            }

            if (extras.containsKey(AppUtils.EXTRAS_ORIGIN_NOTIFICATION)){
                startedByNotification = getIntent().getBooleanExtra(AppUtils.EXTRAS_ORIGIN_NOTIFICATION, false);
            }

            // place directly the low res image from the main activity so there are no delays in the Transition
            // then later we will load the higher res image

            if (extras.containsKey(AppUtils.EXTRAS_LOW_RES_BITMAP)){
                lowResBitmap = getIntent().getParcelableExtra(AppUtils.EXTRAS_LOW_RES_BITMAP);
                if (lowResBitmap!=null)
                    detailActivityImage.setImageBitmap(lowResBitmap);
            }
            supportStartPostponedEnterTransition();

            //detailActivityImage.setImageBitmap(AppUtils.loadImageFromStorage("images", "current_thumb.jpg"));
            //supportStartPostponedEnterTransition();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            detailActivityImage.setTransitionName(Integer.toString(mArticle.getGuid()));
            Log.d("TRANSITION RECEIVED", Integer.toString(mArticle.getGuid()));
        }

        if (mArticle.getImgThumb()!=null) {
            /*
               // Now place the larger image
            if (detailActivityImage != null) {
                Picasso.with(this)
                        .load(mArticle.getImgLarge())
                        .noFade()
                        //.placeholder(detailActivityImage.getDrawable())
                        .into(detailActivityImage, new Callback() {
                            @Override
                            public void onSuccess() {
                                supportStartPostponedEnterTransition();
                            }

                            @Override
                            public void onError() {
                                supportStartPostponedEnterTransition();
                            }
                        });
            }
            */
        }
        else{
            findViewById(R.id.toolbar_layout).setVisibility(View.GONE);
            supportStartPostponedEnterTransition();
        }


        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own detail action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

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

    @Override
    protected void onResume() {
        super.onResume();

        /*
        if (detailActivityImage!=null){
            Picasso.with(this)
                    .load(mArticle.getImgLarge())
                    .into(detailActivityImage);
        }
        */
    }

    /**
     * IF WE DONT MAKE USE OF THE ACTUAL ANDROID TOOLBAR
     * Then we have a drawable arrow icon on the upper left of the poster to act as back button
     * @param view
     */
    public void actionUpClicked(View view) {
        super.onBackPressed();
    }

    /**
     * IF WE MAKE USE OF THE ACTUAL ANDROID TOOLBAR...
     * When the arrow is pressed on the action bar, close the activity
     * by invoking the original "back button pressing". This is to reverse transition
     * @param item
     * @return
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
        super.onBackPressed();
    }
}

package eu.anifantakis.neakriti.data;

import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

import eu.anifantakis.neakriti.R;
import eu.anifantakis.neakriti.data.model.Article;
import eu.anifantakis.neakriti.data.model.ArticlesCollection;
import eu.anifantakis.neakriti.databinding.ArticleListContentBinding;
import eu.anifantakis.neakriti.utils.AppUtils;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class ArticlesListAdapter extends RecyclerView.Adapter<ArticlesListAdapter.ArticleViewHolder> {
    private ArticlesCollection collection;
    private static Picasso picassoCached = null;
    private Activity mActivity;

    final private ArticleItemClickListener mOnClickListener;

    public interface ArticleItemClickListener {
        void onArticleItemClick(int clickedItemIndex, ImageView sharedImage);
    }

    public ArticlesListAdapter(ArticleItemClickListener mOnClickListener) {
        this.mOnClickListener = mOnClickListener;
        mActivity = (Activity) mOnClickListener;

        clearOldFileCache(2);
        if (picassoCached == null) {
            picassoCached = getPicasso();
        }
    }

    public ArticlesListAdapter(ArticleItemClickListener mOnClickListener, Activity activity) {
        this.mOnClickListener = mOnClickListener;
        mActivity = activity;

        clearOldFileCache(2);
        if (picassoCached == null) {
            picassoCached = getPicasso();
        }
    }

    /**
     *
     * @return
     */
    public Picasso getPicasso() {
        // Source: https://gist.github.com/iamtodor/eb7f02fc9571cc705774408a474d5dcb
        OkHttpClient okHttpClient1 = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Response originalResponse = chain.proceed(chain.request());

                        int days=2;
                        long cacheTime = 60 * 60 * 24 * days;

                        return originalResponse.newBuilder().header("Cache-Control", "max-age=" + (cacheTime))
                                .build();
                    }
                })
                .cache(new Cache(mActivity.getCacheDir(), Integer.MAX_VALUE))
                .build();

        OkHttp3Downloader downloader = new OkHttp3Downloader(okHttpClient1);
        Picasso picasso = new Picasso.Builder(mActivity).downloader(downloader).build();
        Picasso.setSingletonInstance(picasso);

        File[] files=mActivity.getCacheDir().listFiles();
        Log.d("FILES IN CACHE", ""+files.length);

        // indicator for checking picasso caching - need to comment out on release
        //picasso.setIndicatorsEnabled(true);

        return picasso;
    }

    /**
     * Clears all the file cache
     */
    public void clearFileCache(){
        File[] files=mActivity.getCacheDir().listFiles();
        for(File f:files)
            f.delete();
    }

    /**
     * Clears the file cache of old files (more than the given days old)
     */
    public void clearOldFileCache(int days){
        File[] files=mActivity.getCacheDir().listFiles();
        for(File f:files) {
            long lastModified = f.lastModified()/1000;
            long currentTime = System.currentTimeMillis()/1000;

            long cacheTime = 60 * 60 * 24 * days;

            if (currentTime-lastModified >= cacheTime)
                f.delete();
        }
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ArticleListContentBinding binding = DataBindingUtil.inflate(inflater, R.layout.article_list_content, parent, false);
        return new ArticleViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        Article article = collection.getArticle(position);

        ViewCompat.setTransitionName (holder.getImageView(), Integer.toString(article.getGuid()));
        holder.setTitle(article.getTitle());
        holder.setImage(article.getImgThumb());
        holder.setDateStr(article.getPubDateStr());
    }

    @Override
    public int getItemCount() {
        if (null == collection) return 0;
        return collection.getCollectionSize();
    }

    public void clearCollection(){
        if (collection!=null) {
            collection.clear();
            notifyDataSetChanged();
        }
    }

    public void setCollection(ArticlesCollection collection){
        this.collection = collection;
        notifyDataSetChanged();
    }

    public Article getArticleAtIndex(int index){
        return collection.getArticle(index);
    }

    public class ArticleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ArticleListContentBinding binding;
        private Context context;

        public ArticleViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            context = itemView.getContext();

            itemView.setOnClickListener(this);
        }

        /**
         * Set the holder article's title
         *
         * @param title
         */
        void setTitle(String title) {
            binding.content.setText(title);
        }

        /**
         * Set the holder movie's thumbnail
         *
         * @param image
         */
        void setImage(String image) {
            if (image==null || image.isEmpty()){
                // if movie has no accompanied backdrop image, load the "no image found" from the drawable folder
                binding.rowIvArticleThumb.setImageResource(R.drawable.backdrop_noimage);
            }else {
                Picasso.with(context)
                        .load(image)
                        .into(binding.rowIvArticleThumb);
            }
        }

        ImageView getImageView(){
            return binding.rowIvArticleThumb;
        }

        void setDateStr(String dateStr){
            binding.listDate.setText(AppUtils.pubDateFormat(dateStr));
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onArticleItemClick(clickedPosition, binding.rowIvArticleThumb);
        }
    }
}

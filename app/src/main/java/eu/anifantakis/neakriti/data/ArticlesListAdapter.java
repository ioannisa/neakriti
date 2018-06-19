package eu.anifantakis.neakriti.data;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;

import eu.anifantakis.neakriti.R;
import eu.anifantakis.neakriti.data.feed.ArticlesCollection;
import eu.anifantakis.neakriti.data.feed.gson.Article;
import eu.anifantakis.neakriti.databinding.RowArticleCardBinding;
import eu.anifantakis.neakriti.databinding.RowArticleListBinding;
import eu.anifantakis.neakriti.utils.AppUtils;

public class ArticlesListAdapter extends RecyclerView.Adapter<ArticlesListAdapter.ArticleViewHolder> {
    private ArticlesCollection collection;
    private Activity mActivity;
    private int selectedPosition = -1;
    private boolean isListView = true;

    final private ArticleItemClickListener mOnClickListener;

    public interface ArticleItemClickListener {
        void onArticleItemClick(int clickedItemIndex, ImageView sharedImage);
    }

    public ArticlesListAdapter(ArticleItemClickListener mOnClickListener) {
        this.mOnClickListener = mOnClickListener;
        mActivity = (Activity) mOnClickListener;

        isListView = PreferenceManager.getDefaultSharedPreferences(mActivity).getBoolean(mActivity.getString(R.string.pref_list_or_card_key), isListView);

        clearOldFileCache(2);
    }

    public ArticlesListAdapter(ArticleItemClickListener mOnClickListener, Activity activity) {
        this.mOnClickListener = mOnClickListener;
        mActivity = activity;

        isListView = PreferenceManager.getDefaultSharedPreferences(mActivity).getBoolean(mActivity.getString(R.string.pref_list_or_card_key), isListView);

        clearOldFileCache(2);
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
    private void clearOldFileCache(int days){
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

        if (isListView){
            RowArticleListBinding binding = DataBindingUtil.inflate(inflater, R.layout.row_article_list, parent, false);
            return new ArticleViewHolder(binding.getRoot());
        }
        else {
            RowArticleCardBinding binding = DataBindingUtil.inflate(inflater, R.layout.row_article_card, parent, false);
            return new ArticleViewHolder(binding.getRoot());
        }

    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        Article article = collection.getArticle(position);

        if (isListView) {
            ViewCompat.setTransitionName(holder.getListImageView(), Integer.toString(article.getGuid()));
            holder.setListTitle(article.getTitle());
            holder.setListImage(article.getImgThumbStr());
            holder.setListDateStr(article.getPubDateStr());
            holder.itemView.setTag(article.getGuid());
            holder.bindingList.articleRow.setSelected((position == selectedPosition));
        }
        else{
            ViewCompat.setTransitionName(holder.getCardImageView(), Integer.toString(article.getGuid()));
            holder.setCardTitle(article.getTitle());
            holder.setCardImage(article.getImgThumbStr());
            holder.setCardDateStr(article.getPubDateStr());
            holder.itemView.setTag(article.getGuid());
            holder.bindingCard.articleRow.setSelected((position == selectedPosition));
        }
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
        Log.d("COLLECTION", "SET DATA");
        this.collection = collection;
        //notifyDataSetChanged();
    }

    public Article getArticleAtIndex(int index){
        return collection.getArticle(index);
    }

    public class ArticleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        RowArticleListBinding bindingList;
        RowArticleCardBinding bindingCard;
        private boolean imageLoaded = false;

        ArticleViewHolder(View itemView) {
            super(itemView);
            if (isListView) {
                bindingList = DataBindingUtil.bind(itemView);
            }
            else{
                bindingCard = DataBindingUtil.bind(itemView);
            }

            itemView.setOnClickListener(this);
        }

        /**
         * Set the holder article's title
         *
         * @param title The Article title
         */
        void setListTitle(String title) {
            bindingList.content.setText(title);
        }
        void setCardTitle(String title) {
            bindingCard.content.setText(title);
        }

        /**
         * Set the holder movie's thumbnail
         *
         * @param image The image thumbnail
         */
        void setCardImage(String image) {
            imageLoaded = false;
            if (image==null || image.isEmpty()){
                // if movie has no accompanied backdrop image, load the "no image found" from the drawable folder
                bindingCard.rowIvArticleThumb.setImageResource(R.drawable.placeholder);
            }else {
                Picasso.get()
                        .load(image)
                        .placeholder(R.drawable.placeholder)
                        .into(bindingCard.rowIvArticleThumb, new Callback(){
                            @Override
                            public void onSuccess() {
                                imageLoaded = true;
                            }

                            @Override
                            public void onError(Exception e) {
                                imageLoaded = false;
                            }
                        });
            }
        }

        void setListImage(String image) {
            imageLoaded = false;
            if (image==null || image.isEmpty()){
                // if movie has no accompanied backdrop image, load the "no image found" from the drawable folder
                bindingList.rowIvArticleThumb.setImageResource(R.drawable.placeholder);
            }else {
                Picasso.get()
                        .load(image)
                        .placeholder(R.drawable.placeholder)
                        .into(bindingList.rowIvArticleThumb, new Callback(){
                            @Override
                            public void onSuccess() {
                                imageLoaded = true;
                            }

                            @Override
                            public void onError(Exception e) {
                                imageLoaded = false;
                            }
                        });
            }
        }

        ImageView getCardImageView(){
            return bindingCard.rowIvArticleThumb;
        }

        ImageView getListImageView(){
            return bindingList.rowIvArticleThumb;
        }

        void setCardDateStr(String dateStr){
            bindingCard.listDate.setText(AppUtils.pubDateFormat(dateStr));
        }

        void setListDateStr(String dateStr){
            bindingList.listDate.setText(AppUtils.pubDateFormat(dateStr));
        }

        @Override
        public void onClick(View view) {
            selectedPosition = getAdapterPosition();
            if (imageLoaded) {
                if (isListView) {
                    mOnClickListener.onArticleItemClick(selectedPosition, bindingList.rowIvArticleThumb);
                }
                else{
                    mOnClickListener.onArticleItemClick(selectedPosition, bindingCard.rowIvArticleThumb);
                }
            }
            else{
                mOnClickListener.onArticleItemClick(selectedPosition, null);
            }
            //notifyDataSetChanged();
        }
    }
}
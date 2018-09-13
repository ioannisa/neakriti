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
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.w3c.dom.Text;

import java.io.File;

import eu.anifantakis.neakriti.R;
import eu.anifantakis.neakriti.data.feed.ArticlesCollection;
import eu.anifantakis.neakriti.data.feed.gson.Article;
import eu.anifantakis.neakriti.databinding.RowArticleCardBinding;
import eu.anifantakis.neakriti.databinding.RowArticleListBinding;
import eu.anifantakis.neakriti.databinding.RowArticleListDetailsBinding;
import eu.anifantakis.neakriti.utils.AppUtils;

public class ArticlesListAdapter extends RecyclerView.Adapter<ArticlesListAdapter.ArticleViewHolder> {
    private ArticlesCollection collection;
    private Activity mActivity;
    private int selectedPosition = -1;

    private int listType;

    final private ArticleItemClickListener mOnClickListener;

    public interface ArticleItemClickListener {
        void onArticleItemClick(int clickedItemIndex, ImageView sharedImage);
    }

    public ArticlesListAdapter(ArticleItemClickListener mOnClickListener) {
        this.mOnClickListener = mOnClickListener;
        mActivity = (Activity) mOnClickListener;

        listType = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(mActivity).getString(mActivity.getString(R.string.pref_list_article_row_appearance),mActivity.getString(R.string.row_type_list_details_id)));

        clearOldFileCache(2);
    }

    public ArticlesListAdapter(ArticleItemClickListener mOnClickListener, Activity activity) {
        this.mOnClickListener = mOnClickListener;
        mActivity = activity;

        listType = PreferenceManager.getDefaultSharedPreferences(mActivity).getInt(mActivity.getString(R.string.pref_list_article_row_appearance),1); //R.string.row_type_list_details_id

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

        //if (listType==2) {
        //  listType=1;
        //}


        if (listType==1){
            RowArticleListBinding binding = DataBindingUtil.inflate(inflater, R.layout.row_article_list, parent, false);
            return new ArticleViewHolder(binding.getRoot());
        }
        else if (listType==2){
            RowArticleListDetailsBinding binding = DataBindingUtil.inflate(inflater, R.layout.row_article_list_details, parent, false);
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

        holder.setTitle(article.getTitle());
        holder.setImage(article.getImgThumbStr());
        holder.setDateStr(article.getPubDateStr());
        holder.itemView.setTag(article.getGuid());

        if (listType==1){
            ViewCompat.setTransitionName(holder.getImageView(), Integer.toString(article.getGuid()));
            holder.bindingList.articleRow.setSelected((position == selectedPosition));
        }
        else if (listType==2){
            ViewCompat.setTransitionName(holder.getImageView(), Integer.toString(article.getGuid()));
            holder.bindingListDetails.articleRow.setSelected((position == selectedPosition));


            // set the preview text
            Document doc = Jsoup.parse(article.getDescription());
            Element firstParagraph = doc.selectFirst("p");

            String previewText = "";
            if (firstParagraph!=null)
                previewText = firstParagraph.text().trim();

            holder.bindingListDetails.articlePreview.setText(previewText);
            holder.bindingListDetails.articlePreview.setMaxLines(4); // 4 max line of preview paragraph text

        }
        else if (listType==3){
            ViewCompat.setTransitionName(holder.getImageView(), Integer.toString(article.getGuid()));
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
        RowArticleListDetailsBinding bindingListDetails;
        RowArticleCardBinding bindingCard;
        private boolean imageLoaded = false;

        ArticleViewHolder(View itemView) {
            super(itemView);

            if (listType==1) bindingList = DataBindingUtil.bind(itemView); else
            if (listType==2) bindingListDetails = DataBindingUtil.bind(itemView); else
            if (listType==3) bindingCard = DataBindingUtil.bind(itemView);

            itemView.setOnClickListener(this);
        }

        /**
         * Set the holder article's title
         *
         * @param title The Article title
         */
        void setTitle(String title) {
            if (listType==1) bindingList.content.setText(title); else
            if (listType==2) bindingListDetails.content.setText(title); else
            if (listType==3) bindingCard.content.setText(title);
        }

        /**
         * Set the holder movie's thumbnail
         *
         * @param image The image thumbnail
         */
        void setImage(String image) {
            imageLoaded = false;

            ImageView target;
            if (listType==1) target = bindingList.rowIvArticleThumb; else
            if (listType==2) target = bindingListDetails.rowIvArticleThumb; else
            target = bindingCard.rowIvArticleThumb;

            if (image==null || image.isEmpty()){
                // if movie has no accompanied backdrop image, load the "no image found" from the drawable folder
                target.setImageResource(R.drawable.placeholder);
            }else {
                Picasso.get()
                        .load(image)
                        .placeholder(R.drawable.placeholder)

                        .into(target, new Callback(){
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

        ImageView getImageView(){
            ImageView target;
            if (listType==1) target = bindingList.rowIvArticleThumb; else
            if (listType==2) target = bindingListDetails.rowIvArticleThumb; else
            target = bindingCard.rowIvArticleThumb;

            return target;
        }

        void setDateStr(String dateStr){
            TextView target;
            if (listType==1) target = bindingList.listDate; else
            if (listType==2) target = bindingListDetails.listDate; else
            target = bindingCard.listDate;

            target.setText(AppUtils.pubDateFormat(dateStr));
        }

        @Override
        public void onClick(View view) {
            selectedPosition = getAdapterPosition();
            if (imageLoaded) {
                if (listType==1) mOnClickListener.onArticleItemClick(selectedPosition, bindingList.rowIvArticleThumb); else
                if (listType==2) mOnClickListener.onArticleItemClick(selectedPosition, bindingListDetails.rowIvArticleThumb); else
                if (listType==3) mOnClickListener.onArticleItemClick(selectedPosition, bindingCard.rowIvArticleThumb);
            }
            else{
                mOnClickListener.onArticleItemClick(selectedPosition, null);
            }
            //notifyDataSetChanged();
        }
    }
}
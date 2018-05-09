package eu.anifantakis.neakriti.data;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import eu.anifantakis.neakriti.R;
import eu.anifantakis.neakriti.data.model.Article;
import eu.anifantakis.neakriti.data.model.ArticlesCollection;
import eu.anifantakis.neakriti.databinding.ArticleListContentBinding;

public class ArticlesListAdapter extends RecyclerView.Adapter<ArticlesListAdapter.ArticleViewHolder> {
    private ArticlesCollection collection;
    final private ArticleItemClickListener mOnClickListener;

    public interface ArticleItemClickListener {
        void onArticleItemClick(int clickedItemIndex);
    }

    public ArticlesListAdapter(ArticleItemClickListener mOnClickListener) {
        this.mOnClickListener = mOnClickListener;
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
        holder.setTitle(collection.getArticle(position).getTitle());
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
        }

        /**
         * Set the holder article's title
         *
         * @param title
         */
        void setTitle(String title) {
            binding.content.setText(title);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onArticleItemClick(clickedPosition);
        }
    }
}

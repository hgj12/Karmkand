package com.karmkand.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.karmkand.app.R;
import com.karmkand.app.Utils.SqlLiteDbHelper.SearchResult;
import com.karmkand.app.Utils.Utility;

import java.util.ArrayList;
import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    public interface OnSearchResultClickListener {
        void onResultClick(SearchResult result);
    }

    private final Context context;
    private final List<SearchResult> results = new ArrayList<>();
    private final OnSearchResultClickListener listener;

    public SearchResultAdapter(Context context, OnSearchResultClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void updateData(List<SearchResult> newResults) {
        results.clear();
        if (newResults != null) {
            results.addAll(newResults);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchResult result = results.get(position);
        holder.tvResultTitle.setText(result.title);
        holder.tvResultSnippet.setText(result.subtitle != null ? result.subtitle : "");
        holder.tvResultBadge.setText(result.categoryName);

        if (result.categoryIndex == Utility.MAINCATEGORY.AARTI.ordinal()) {
            holder.ivResultType.setImageResource(R.drawable.ic_diya);
        } else if (result.categoryIndex == Utility.MAINCATEGORY.HOME.ordinal()) {
            holder.ivResultType.setImageResource(R.drawable.ic_havan);
        } else if (result.categoryIndex == Utility.MAINCATEGORY.RAJOPACHARPUJA.ordinal()) {
            holder.ivResultType.setImageResource(R.drawable.ic_puja_thali);
        } else {
            holder.ivResultType.setImageResource(R.drawable.ic_nav_mantra);
        }

        holder.cardSearchResult.setOnClickListener(v -> {
            if (listener != null) {
                listener.onResultClick(result);
            }
        });
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardSearchResult;
        AppCompatImageView ivResultType;
        AppCompatTextView tvResultTitle, tvResultSnippet, tvResultBadge;

        ViewHolder(View itemView) {
            super(itemView);
            cardSearchResult = itemView.findViewById(R.id.cardSearchResult);
            ivResultType = itemView.findViewById(R.id.ivResultType);
            tvResultTitle = itemView.findViewById(R.id.tvResultTitle);
            tvResultSnippet = itemView.findViewById(R.id.tvResultSnippet);
            tvResultBadge = itemView.findViewById(R.id.tvResultBadge);
        }
    }
}

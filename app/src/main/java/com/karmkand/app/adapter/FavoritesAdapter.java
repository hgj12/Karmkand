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
import com.karmkand.app.Utils.FavoritesManager.FavoriteItem;
import com.karmkand.app.Utils.Utility;

import java.util.ArrayList;
import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {

    public interface OnFavoriteClickListener {
        void onFavoriteClick(FavoriteItem item);
    }

    private final Context context;
    private final List<FavoriteItem> items = new ArrayList<>();
    private final OnFavoriteClickListener listener;

    public FavoritesAdapter(Context context, OnFavoriteClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void updateData(List<FavoriteItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
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
        FavoriteItem item = items.get(position);
        holder.tvResultTitle.setText(item.title);
        holder.tvResultSnippet.setText(item.headerName != null && !item.headerName.isEmpty() ? item.headerName : item.categoryName);
        holder.tvResultBadge.setText(item.categoryName);

        if (item.categoryIndex == Utility.MAINCATEGORY.AARTI.ordinal()) {
            holder.ivResultType.setImageResource(R.drawable.ic_diya);
        } else if (item.categoryIndex == Utility.MAINCATEGORY.HOME.ordinal()) {
            holder.ivResultType.setImageResource(R.drawable.ic_havan);
        } else if (item.categoryIndex == Utility.MAINCATEGORY.RAJOPACHARPUJA.ordinal()) {
            holder.ivResultType.setImageResource(R.drawable.ic_puja_thali);
        } else {
            holder.ivResultType.setImageResource(R.drawable.ic_nav_mantra);
        }

        holder.cardSearchResult.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFavoriteClick(item);
            }
        });
    }


    @Override
    public int getItemCount() {
        return items.size();
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

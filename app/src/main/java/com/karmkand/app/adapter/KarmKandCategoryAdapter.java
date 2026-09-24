package com.karmkand.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.karmkand.app.R;
import com.karmkand.app.Utils.LocaleStringHelper;
import com.karmkand.app.initialize.AartiData;
import com.karmkand.app.listener.CategoryClickListener;

import java.util.ArrayList;
import java.util.List;

public class KarmKandCategoryAdapter extends RecyclerView.Adapter<KarmKandCategoryAdapter.ViewHolder> {

    private final Context context;
    private final List<Object> originalList = new ArrayList<>();
    private final List<Object> displayList = new ArrayList<>();
    private CategoryClickListener categoryClickListener;

    public KarmKandCategoryAdapter(Context context, List<Object> slokdetail) {
        this.context = context;
        if (slokdetail != null) {
            this.originalList.addAll(slokdetail);
            this.displayList.addAll(slokdetail);
        }
    }

    public void filter(String query) {
        displayList.clear();
        if (query == null || query.trim().isEmpty()) {
            displayList.addAll(originalList);
        } else {
            String lower = query.trim().toLowerCase();
            for (Object obj : originalList) {
                String title = "";
                if (obj instanceof AartiData) {
                    title = ((AartiData) obj).getName();
                } else if (obj != null) {
                    title = String.valueOf(obj);
                }
                if (title != null && title.toLowerCase().contains(lower)) {
                    displayList.add(obj);
                }
            }
        }
        notifyDataSetChanged();
    }

    public Object getItem(int position) {
        if (position >= 0 && position < displayList.size()) {
            return displayList.get(position);
        }
        return null;
    }

    public int getOriginalPosition(int position) {
        Object item = getItem(position);
        if (item != null) {
            return originalList.indexOf(item);
        }
        return position;
    }

    @NonNull
    @Override
    public KarmKandCategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new KarmKandCategoryAdapter.ViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false)
        );
    }

    public void setCategoryClickListener(CategoryClickListener categoryClickListener) {
        this.categoryClickListener = categoryClickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        AppCompatTextView tvTitleMain, txtItemIndex;
        CardView cardView;

        private ViewHolder(View v) {
            super(v);
            tvTitleMain = v.findViewById(R.id.tvSlokHeader);
            txtItemIndex = v.findViewById(R.id.txtItemIndex);
            cardView = v.findViewById(R.id.card_view);
            cardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (categoryClickListener != null) {
                int originalPos = getOriginalPosition(getLayoutPosition());
                categoryClickListener.categoryItemClick(originalPos);
            }
        }
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        Object object = displayList.get(position);
        if (holder.txtItemIndex != null) {
            holder.txtItemIndex.setText(String.valueOf(position + 1));
        }

        if (object instanceof AartiData) {
            AartiData aartiData = (AartiData) object;
            LocaleStringHelper.setText(
                    holder.tvTitleMain,
                    aartiData.getName(),
                    R.string.fallback_list_item
            );
        } else {
            LocaleStringHelper.setText(
                    holder.tvTitleMain,
                    String.valueOf(object),
                    R.string.fallback_list_item
            );
        }
    }
}
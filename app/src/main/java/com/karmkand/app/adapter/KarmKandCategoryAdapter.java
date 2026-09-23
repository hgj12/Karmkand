package com.karmkand.app.adapter;


import android.content.Context;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.karmkand.app.R;
import com.karmkand.app.Utils.LocaleStringHelper;
import com.karmkand.app.initialize.AartiData;
import com.karmkand.app.listener.CategoryClickListener;

import java.util.ArrayList;
import java.util.List;


public class KarmKandCategoryAdapter extends RecyclerView.Adapter<KarmKandCategoryAdapter.ViewHolder> {

    private Context context;
    private List<Object> slokList = new ArrayList<Object>();
    private CategoryClickListener categoryClickListener;

    public KarmKandCategoryAdapter(Context context, List<Object> slokdetail) {
        this.context = context;
        this.slokList.addAll(slokdetail);

    }

    @NonNull
    @Override
    public KarmKandCategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new KarmKandCategoryAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false));
    }

    public void setCategoryClickListener(CategoryClickListener categoryClickListener) {
        this.categoryClickListener = categoryClickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        AppCompatTextView tvTitleMain;
        CardView cardView;

        private ViewHolder(View v) {
            super(v);
            tvTitleMain = v.findViewById(R.id.tvSlokHeader);
            cardView = v.findViewById(R.id.card_view);
            cardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            if (categoryClickListener != null) {
                categoryClickListener.categoryItemClick(getLayoutPosition());
            }

        }
    }

    @Override
    public int getItemCount() {
        return slokList.size();
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        Object object = slokList.get(position);
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
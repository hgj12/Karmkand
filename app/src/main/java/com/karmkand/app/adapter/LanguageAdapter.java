package com.karmkand.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.karmkand.app.R;
import com.karmkand.app.Utils.LocaleManager;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.ViewHolder> {

    public interface OnLanguageSelectedListener {
        void onLanguageSelected(String languageCode);
    }

    private final Context context;
    private final String[] codes;
    private final String[] displayNames;
    private int selectedIndex;
    private final OnLanguageSelectedListener listener;

    private static final String[] ENGLISH_NAMES = {
            "Hindi", "English", "Gujarati", "Marathi", "Bengali", "Kannada", "Malayalam", "Tamil", "Telugu"
    };

    public LanguageAdapter(Context context, OnLanguageSelectedListener listener) {
        this.context = context;
        this.codes = LocaleManager.getLanguageCodes();
        this.displayNames = LocaleManager.getLanguageDisplayNames();
        this.selectedIndex = LocaleManager.getSelectedIndex(context);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_language, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvLangNativeName.setText(displayNames[position]);
        holder.tvLangEnglishName.setText(position < ENGLISH_NAMES.length ? ENGLISH_NAMES[position] : "");
        holder.rbLangSelected.setChecked(position == selectedIndex);

        if (position == selectedIndex) {
            holder.tvLangNativeName.setTextColor(context.getResources().getColor(R.color.gold_light));
        } else {
            holder.tvLangNativeName.setTextColor(context.getResources().getColor(R.color.white));
        }

        holder.itemView.setOnClickListener(v -> {
            int oldSelected = selectedIndex;
            selectedIndex = holder.getAdapterPosition();
            notifyItemChanged(oldSelected);
            notifyItemChanged(selectedIndex);
            if (listener != null) {
                listener.onLanguageSelected(codes[selectedIndex]);
            }
        });
    }

    @Override
    public int getItemCount() {
        return codes.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        AppCompatTextView tvLangNativeName, tvLangEnglishName;
        AppCompatRadioButton rbLangSelected;

        ViewHolder(View itemView) {
            super(itemView);
            tvLangNativeName = itemView.findViewById(R.id.tvLangNativeName);
            tvLangEnglishName = itemView.findViewById(R.id.tvLangEnglishName);
            rbLangSelected = itemView.findViewById(R.id.rbLangSelected);
        }
    }
}

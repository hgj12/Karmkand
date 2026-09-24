package com.karmkand.app.Utils;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.karmkand.app.KarmKandMenu;
import com.karmkand.app.R;
import com.karmkand.app.adapter.LanguageAdapter;

public class LanguagePickerDialog {

    public static void show(AppCompatActivity activity) {
        Dialog dialog = new Dialog(activity, R.style.SpiritualDialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_language_picker, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        RecyclerView rv = view.findViewById(R.id.rvLanguages);
        rv.setLayoutManager(new LinearLayoutManager(activity));

        LanguageAdapter adapter = new LanguageAdapter(activity, languageCode -> {
            if (!languageCode.equals(LocaleManager.getSavedLanguage(activity))) {
                LocaleManager.saveLanguage(activity, languageCode);
                LocaleManager.applyLanguage(languageCode);
                dialog.dismiss();

                Intent intent = new Intent(activity, KarmKandMenu.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                activity.startActivity(intent);
                activity.finish();
            } else {
                dialog.dismiss();
            }
        });

        rv.setAdapter(adapter);

        View btnClose = view.findViewById(R.id.btnLangClose);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }
}

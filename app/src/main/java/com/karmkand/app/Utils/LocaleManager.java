package com.karmkand.app.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

/**
 * Persists and applies the in-app UI language. Default is Hindi ({@value #LANGUAGE_DEFAULT}).
 */
public final class LocaleManager {

    public static final String PREF_LANGUAGE = "app_language";
    /** @deprecated Use {@link #LANGUAGE_DEFAULT}; kept for migrating old prefs. */
    @Deprecated
    public static final String LANGUAGE_SYSTEM = "system";
    public static final String LANGUAGE_DEFAULT = "hi";

    private static final String PREFS_NAME = "karmkand_locale";

    private static final String[] LANGUAGE_CODES = {
            LANGUAGE_DEFAULT,
            "en",
            "gu",
            "mr",
            "bn",
            "kn",
            "ml",
            "ta",
            "te"
    };

    private LocaleManager() {
    }

    @NonNull
    public static String[] getLanguageCodes() {
        return LANGUAGE_CODES.clone();
    }

    @NonNull
    public static String[] getLanguageDisplayNames() {
        return new String[]{
                "हिन्दी (Hindi)",
                "English",
                "ગુજરાતી (Gujarati)",
                "मराठी (Marathi)",
                "বাংলা (Bengali)",
                "ಕನ್ನಡ (Kannada)",
                "മലയാളം (Malayalam)",
                "தமிழ் (Tamil)",
                "తెలుగు (Telugu)"
        };
    }

    @NonNull
    public static String getSavedLanguage(@NonNull Context context) {
        String saved = getPrefs(context).getString(PREF_LANGUAGE, LANGUAGE_DEFAULT);
        if (LANGUAGE_SYSTEM.equals(saved) || TextUtils.isEmpty(saved)) {
            return LANGUAGE_DEFAULT;
        }
        return saved;
    }

    public static void saveLanguage(@NonNull Context context, @NonNull String languageCode) {
        String code = LANGUAGE_SYSTEM.equals(languageCode)
                || TextUtils.isEmpty(languageCode)
                ? LANGUAGE_DEFAULT
                : languageCode;
        getPrefs(context).edit().putString(PREF_LANGUAGE, code).apply();
    }

    public static void applySavedLanguage(@NonNull Context context) {
        applyLanguage(getSavedLanguage(context));
    }

    public static void applyLanguage(@Nullable String languageCode) {
        String code = languageCode;
        if (TextUtils.isEmpty(code) || LANGUAGE_SYSTEM.equals(code)) {
            code = LANGUAGE_DEFAULT;
        }
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code));
        LocaleStringHelper.reset();
    }

    public static int getSelectedIndex(@NonNull Context context) {
        String saved = getSavedLanguage(context);
        for (int i = 0; i < LANGUAGE_CODES.length; i++) {
            if (LANGUAGE_CODES[i].equals(saved)) {
                return i;
            }
        }
        return 0;
    }

    @NonNull
    private static SharedPreferences getPrefs(@NonNull Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}

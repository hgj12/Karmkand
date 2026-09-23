package com.karmkand.app.Utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.karmkand.app.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Resolves UI strings with Hindi fallback so labels never render blank or as raw resource keys.
 */
public final class LocaleStringHelper {

    private static Resources fallbackResources;
    @Nullable
    private static String fallbackLanguageTag;

    private LocaleStringHelper() {
    }

    public static void init(@NonNull Context context) {
        String languageTag = resolveFallbackLanguageTag(context);
        if (fallbackResources != null && languageTag.equals(fallbackLanguageTag)) {
            return;
        }
        fallbackLanguageTag = languageTag;
        fallbackResources = buildFallbackResources(context, languageTag);
    }

    public static void reset() {
        fallbackResources = null;
        fallbackLanguageTag = null;
    }

    @NonNull
    private static String resolveFallbackLanguageTag(@NonNull Context context) {
        return "hi";
    }

    @NonNull
    private static Resources buildFallbackResources(
            @NonNull Context context,
            @NonNull String languageTag
    ) {
        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(Locale.forLanguageTag(languageTag));
        return context.getApplicationContext()
                .createConfigurationContext(config)
                .getResources();
    }

    @NonNull
    public static String getString(@NonNull Context context, @StringRes int resId) {
        init(context);
        String localized = safeRaw(context.getResources().getString(resId));
        String fallback = safeRaw(fallbackResources.getString(resId));
        return coalesce(localized, fallback, context.getString(R.string.fallback_content_unavailable));
    }

    @NonNull
    public static String getString(@NonNull Context context, @StringRes int resId, Object... formatArgs) {
        String base = getString(context, resId);
        if (formatArgs != null && formatArgs.length > 0) {
            try {
                return String.format(Locale.getDefault(), base, formatArgs);
            } catch (Exception e) {
                return base;
            }
        }
        return base;
    }

    @NonNull
    public static String[] getStringArray(@NonNull Context context, int arrayResId) {
        init(context);
        String[] localized = context.getResources().getStringArray(arrayResId);
        String[] fallbackItems;
        try {
            fallbackItems = fallbackResources.getStringArray(arrayResId);
        } catch (Resources.NotFoundException e) {
            fallbackItems = new String[0];
        }

        int length = Math.max(localized.length, fallbackItems.length);
        String[] resolved = new String[length];
        String itemFallback = getString(context, R.string.fallback_list_item);

        for (int i = 0; i < length; i++) {
            String primary = i < localized.length ? localized[i] : null;
            String fallback = i < fallbackItems.length ? fallbackItems[i] : null;
            resolved[i] = coalesce(primary, fallback, itemFallback);
        }
        return resolved;
    }

    @NonNull
    public static List<String> getStringArrayList(@NonNull Context context, int arrayResId) {
        return new ArrayList<>(Arrays.asList(getStringArray(context, arrayResId)));
    }

    @NonNull
    public static String getStringArrayItem(
            @NonNull Context context,
            int arrayResId,
            int index,
            @StringRes int fallbackResId
    ) {
        String[] items = getStringArray(context, arrayResId);
        if (index >= 0 && index < items.length) {
            String item = items[index];
            if (!isInvalidDisplayText(item)) {
                return item;
            }
        }
        return getString(context, fallbackResId);
    }

    @NonNull
    public static String resolveDisplayText(
            @NonNull Context context,
            @Nullable String candidate,
            @StringRes int fallbackResId
    ) {
        if (!isInvalidDisplayText(candidate)) {
            return candidate.trim();
        }
        return getString(context, fallbackResId);
    }

    public static void setText(@NonNull TextView textView, @StringRes int resId) {
        textView.setText(getString(textView.getContext(), resId));
    }

    public static void setText(
            @NonNull TextView textView,
            @Nullable String candidate,
            @StringRes int fallbackResId
    ) {
        textView.setText(resolveDisplayText(textView.getContext(), candidate, fallbackResId));
    }

    public static boolean isInvalidDisplayText(@Nullable String text) {
        if (text == null) {
            return true;
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return true;
        }
        if (trimmed.startsWith("@string/") || trimmed.startsWith("@array/")) {
            return true;
        }
        if (trimmed.matches("^@\\w+/[\\w.]+$")) {
            return true;
        }
        if (looksLikeResourceKey(trimmed)) {
            return true;
        }
        return false;
    }

    @NonNull
    private static String coalesce(
            @Nullable String primary,
            @Nullable String fallback,
            @NonNull String lastResort
    ) {
        if (!isInvalidDisplayText(primary)) {
            return primary.trim();
        }
        if (!isInvalidDisplayText(fallback)) {
            return fallback.trim();
        }
        return lastResort;
    }

    @Nullable
    private static String safeRaw(@Nullable String value) {
        return value == null ? null : value;
    }

    private static boolean looksLikeResourceKey(@NonNull String text) {
        if (!text.matches("^[a-z][a-z0-9_]*$")) {
            return false;
        }
        return text.startsWith("category_")
                || text.startsWith("drawer_")
                || text.startsWith("sandhya_")
                || text.startsWith("mantra_")
                || text.startsWith("rudri_")
                || text.contains("_title")
                || text.contains("_list");
    }
}

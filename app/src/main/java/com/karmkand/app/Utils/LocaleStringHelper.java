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
            String primary = i < localized.length ? cleanDisplayText(localized[i]) : null;
            String fallback = i < fallbackItems.length ? cleanDisplayText(fallbackItems[i]) : null;
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
            String item = cleanDisplayText(items[index]);
            if (!isInvalidDisplayText(item)) {
                return item;
            }
        }
        return getString(context, fallbackResId);
    }

    /**
     * Cleans display text by stripping outer quotation marks, leading/trailing decorative delimiters
     * (such as dots, hyphens, bullets, pipes, or tildes used purely as border framing), legacy header
     * banners, and normalizing spaces.
     *
     * Legitimate devotional sentence terminators (dandas '।', '॥') and Latin sentence periods are preserved.
     */
    @Nullable
    public static String cleanDisplayText(@Nullable String text) {
        if (text == null) {
            return null;
        }
        String cleaned = text.trim();
        if (cleaned.isEmpty()) {
            return "";
        }

        // 1. Remove surrounding double quotes or single quotes if wrapped
        if ((cleaned.startsWith("\"") && cleaned.endsWith("\"") && cleaned.length() > 1)
                || (cleaned.startsWith("'") && cleaned.endsWith("'") && cleaned.length() > 1)) {
            cleaned = cleaned.substring(1, cleaned.length() - 1).trim();
        }

        // 2. Remove legacy header banner artifacts (e.g. Aarti1 \n -------)
        cleaned = cleaned.replaceAll("(?i)^\\s*Aarti\\d*\\s*\\n+[-=\\s]+\\n*", "");
        cleaned = cleaned.replaceAll("(?i)\\n+\\s*[-=]{3,}\\s*\\n+\\s*Aarti\\d*\\s*\\n+\\s*[-=]{3,}\\s*\\n+", "\n\n");
        cleaned = cleaned.replaceAll("\\n+\\s*[-=]{3,}\\s*\\([Ee]xtra\\)\\s*[-=]{3,}\\s*\\n+", "\n\n");
        cleaned = cleaned.replaceAll("\\n+\\s*[-=]{3,}\\s*\\n+", "\n\n");
        cleaned = cleaned.replaceAll("(?i)Aarti shri Gayatri ji ki", "आरती श्री गायत्री जी की");

        // 3. Strip leading decorative delimiters: dots, dashes, bullets, pipes, tildes, asterisks
        cleaned = cleaned.replaceAll("^[\\s\\.\\-•*|~]+", "");

        // 4. Strip trailing decorative delimiters
        cleaned = cleaned.replaceAll("[\\s\\-•*|~]+$", "");
        cleaned = cleaned.replaceAll("\\.{2,}$", "");
        cleaned = cleaned.replaceAll("\\s+\\.$", "");

        // If the string contains Indic characters or ends with dot following a non-latin character, strip trailing dot
        if (cleaned.endsWith(".") && !cleaned.matches(".*[a-zA-Z0-9]\\.$")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }

        cleaned = cleaned.trim();
        return cleaned;
    }

    @NonNull
    public static String resolveDisplayText(
            @NonNull Context context,
            @Nullable String candidate,
            @StringRes int fallbackResId
    ) {
        String cleaned = cleanDisplayText(candidate);
        if (!isInvalidDisplayText(cleaned)) {
            return cleaned;
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
        if ("null".equalsIgnoreCase(trimmed) || "undefined".equalsIgnoreCase(trimmed)) {
            return true;
        }
        if (trimmed.equals("%s") || trimmed.equals("{name}") || trimmed.equals("%1$s") || trimmed.equals("%2$s")) {
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
        String cleanedPrimary = cleanDisplayText(primary);
        if (!isInvalidDisplayText(cleanedPrimary)) {
            return cleanedPrimary;
        }
        String cleanedFallback = cleanDisplayText(fallback);
        if (!isInvalidDisplayText(cleanedFallback)) {
            return cleanedFallback;
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

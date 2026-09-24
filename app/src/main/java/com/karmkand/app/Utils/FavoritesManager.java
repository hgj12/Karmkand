package com.karmkand.app.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FavoritesManager {

    private static final String PREFS_NAME = "karmkand_favorites";
    private static final String KEY_ITEMS = "favorite_items";

    public static class FavoriteItem implements Serializable {
        public String key;
        public String title;
        public String categoryName;
        public int categoryIndex;
        public String headerName;
        public int aartiId;

        public FavoriteItem(String key, String title, String categoryName, int categoryIndex, String headerName, int aartiId) {
            this.key = key;
            this.title = title;
            this.categoryName = categoryName;
            this.categoryIndex = categoryIndex;
            this.headerName = headerName;
            this.aartiId = aartiId;
        }

        public JSONObject toJson() {
            try {
                JSONObject obj = new JSONObject();
                obj.put("key", key);
                obj.put("title", title);
                obj.put("categoryName", categoryName);
                obj.put("categoryIndex", categoryIndex);
                obj.put("headerName", headerName != null ? headerName : "");
                obj.put("aartiId", aartiId);
                return obj;
            } catch (Exception e) {
                return null;
            }
        }

        public static FavoriteItem fromJson(JSONObject obj) {
            if (obj == null) return null;
            return new FavoriteItem(
                    obj.optString("key", ""),
                    obj.optString("title", ""),
                    obj.optString("categoryName", ""),
                    obj.optInt("categoryIndex", 0),
                    obj.optString("headerName", ""),
                    obj.optInt("aartiId", -1)
            );
        }
    }

    public static List<FavoriteItem> getFavorites(Context context) {
        List<FavoriteItem> list = new ArrayList<>();
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String raw = sp.getString(KEY_ITEMS, "[]");
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                FavoriteItem item = FavoriteItem.fromJson(arr.getJSONObject(i));
                if (item != null && item.key != null && !item.key.isEmpty()) {
                    list.add(item);
                }
            }
        } catch (Exception e) {
            Utility.getInstance().showLog(e);
        }
        return list;
    }

    public static boolean isFavorite(Context context, String key) {
        if (key == null || key.isEmpty()) return false;
        List<FavoriteItem> list = getFavorites(context);
        for (FavoriteItem item : list) {
            if (key.equals(item.key)) {
                return true;
            }
        }
        return false;
    }

    public static void toggleFavorite(Context context, FavoriteItem item) {
        if (item == null || item.key == null) return;
        List<FavoriteItem> list = getFavorites(context);
        int index = -1;
        for (int i = 0; i < list.size(); i++) {
            if (item.key.equals(list.get(i).key)) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            list.remove(index);
        } else {
            list.add(0, item);
        }
        saveFavorites(context, list);
    }

    private static void saveFavorites(Context context, List<FavoriteItem> list) {
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        JSONArray arr = new JSONArray();
        for (FavoriteItem item : list) {
            JSONObject obj = item.toJson();
            if (obj != null) {
                arr.put(obj);
            }
        }
        sp.edit().putString(KEY_ITEMS, arr.toString()).apply();
    }
}

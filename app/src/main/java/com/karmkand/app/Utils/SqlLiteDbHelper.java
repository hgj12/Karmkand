package com.karmkand.app.Utils;


import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.karmkand.app.initialize.AartiData;
import com.karmkand.app.initialize.SlokList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class SqlLiteDbHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 4;

    // Database Name
    private static final String DATABASE_NAME = "MantraVidhiDB";

    // Primary Tables
    private static final String TABLE_MAIN = "tbl_mantras";
    private static final String TABLE_AARTI = "tbl_aarti";

    // Translation Tables
    private static final String TABLE_MANTRAS_TRANSLATION = "tbl_mantras_translation";
    private static final String TABLE_AARTI_TRANSLATION = "tbl_aarti_translation";

    private SQLiteDatabase db;
    private final Context ctx;

    public SqlLiteDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        ctx = context;
    }

    public void CopyDataBaseFromAsset() throws IOException {
        File dbFile = ctx.getDatabasePath(DATABASE_NAME);
        if (dbFile.exists() && dbFile.length() > 0) {
            return;
        }

        File dbDir = dbFile.getParentFile();
        if (dbDir != null && !dbDir.exists()) {
            dbDir.mkdirs();
        }

        try (InputStream in = ctx.getAssets().open(DATABASE_NAME);
             OutputStream out = new FileOutputStream(dbFile)) {
            byte[] buffer = new byte[2048];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            out.flush();
        }
    }

    public void openDataBase() throws SQLException {
        File dbFile = ctx.getDatabasePath(DATABASE_NAME);
        if (db == null || !db.isOpen()) {
            db = SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.CREATE_IF_NECESSARY);
        }
    }

    @Override
    public synchronized void close() {
        if (db != null && db.isOpen()) {
            db.close();
            db = null;
        }
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Base database is preloaded from assets.
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            upgradeToVersion4(db);
        }
    }

    private void upgradeToVersion4(SQLiteDatabase db) {
        // Create translation tables and indexes if they do not exist
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_MANTRAS_TRANSLATION + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "mantra_id INTEGER NOT NULL, " +
                "language_code TEXT NOT NULL, " +
                "slok_header TEXT NOT NULL, " +
                "slok_name TEXT NOT NULL, " +
                "slok TEXT NOT NULL, " +
                "FOREIGN KEY (mantra_id) REFERENCES " + TABLE_MAIN + "(ID) ON DELETE CASCADE, " +
                "UNIQUE(mantra_id, language_code)" +
                ");");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_mantras_trans_lang_header ON " + TABLE_MANTRAS_TRANSLATION + " (language_code, slok_header);");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_mantras_trans_mantra_id ON " + TABLE_MANTRAS_TRANSLATION + " (mantra_id);");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_AARTI_TRANSLATION + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "aarti_id INTEGER NOT NULL, " +
                "language_code TEXT NOT NULL, " +
                "name TEXT NOT NULL, " +
                "lyrics TEXT NOT NULL, " +
                "FOREIGN KEY (aarti_id) REFERENCES " + TABLE_AARTI + "(id) ON DELETE CASCADE, " +
                "UNIQUE(aarti_id, language_code)" +
                ");");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_aarti_trans_lang_id ON " + TABLE_AARTI_TRANSLATION + " (language_code, aarti_id);");

        // Copy localized data from asset database seed
        File tempSeed = new File(ctx.getCacheDir(), "MantraVidhiDB_seed.db");
        try {
            try (InputStream in = ctx.getAssets().open(DATABASE_NAME);
                 OutputStream out = new FileOutputStream(tempSeed)) {
                byte[] buffer = new byte[2048];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
                out.flush();
            }

            SQLiteDatabase seedDb = SQLiteDatabase.openDatabase(
                    tempSeed.getPath(),
                    null,
                    SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS
            );

            // Import mantras translation
            try (Cursor c = seedDb.rawQuery("SELECT id, mantra_id, language_code, slok_header, slok_name, slok FROM " + TABLE_MANTRAS_TRANSLATION, null)) {
                android.database.sqlite.SQLiteStatement stmt = db.compileStatement(
                        "INSERT OR REPLACE INTO " + TABLE_MANTRAS_TRANSLATION + " (id, mantra_id, language_code, slok_header, slok_name, slok) VALUES (?, ?, ?, ?, ?, ?)"
                );
                while (c.moveToNext()) {
                    stmt.bindLong(1, c.getLong(0));
                    stmt.bindLong(2, c.getLong(1));
                    stmt.bindString(3, c.getString(2));
                    stmt.bindString(4, c.getString(3));
                    stmt.bindString(5, c.getString(4));
                    stmt.bindString(6, c.getString(5));
                    stmt.executeInsert();
                    stmt.clearBindings();
                }
            }

            // Import aarti translation
            try (Cursor c = seedDb.rawQuery("SELECT id, aarti_id, language_code, name, lyrics FROM " + TABLE_AARTI_TRANSLATION, null)) {
                android.database.sqlite.SQLiteStatement stmt = db.compileStatement(
                        "INSERT OR REPLACE INTO " + TABLE_AARTI_TRANSLATION + " (id, aarti_id, language_code, name, lyrics) VALUES (?, ?, ?, ?, ?)"
                );
                while (c.moveToNext()) {
                    stmt.bindLong(1, c.getLong(0));
                    stmt.bindLong(2, c.getLong(1));
                    stmt.bindString(3, c.getString(2));
                    stmt.bindString(4, c.getString(3));
                    stmt.bindString(5, c.getString(4));
                    stmt.executeInsert();
                    stmt.clearBindings();
                }
            }

            // Update fallback tbl_mantras headers/names to remove dots
            db.execSQL("UPDATE " + TABLE_MAIN + " SET " +
                    "slok_header = TRIM(REPLACE(slok_header, '.', '')), " +
                    "slok_name = TRIM(REPLACE(slok_name, '.', ''));");

            seedDb.close();
        } catch (Exception e) {
            Utility.getInstance().showLog(e);
        } finally {
            if (tempSeed.exists()) {
                tempSeed.delete();
            }
        }
    }

    /**
     * Get all categories for a given category name, localized to the user's active language.
     *
     * @param catName category Name (e.g. "avahan", "hom", "rajopchar")
     * @return list of localized slok_headers
     */
    public List<String> getCategory(String catName) {
        return getCategory(catName, LocaleManager.getSavedLanguage(ctx));
    }

    public List<String> getCategory(String catName, String languageCode) {
        ArrayList<String> categoryList = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();
        String query = "SELECT COALESCE(t.slok_header, m.slok_header) AS header " +
                "FROM " + TABLE_MAIN + " m " +
                "LEFT JOIN " + TABLE_MANTRAS_TRANSLATION + " t " +
                "  ON m.ID = t.mantra_id AND t.language_code = ? " +
                "WHERE m.slok_category = ? " +
                "GROUP BY m.slok_header " +
                "ORDER BY MIN(m.ID) ASC";
        try (Cursor cursor = database.rawQuery(query, new String[]{languageCode, catName})) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String rawHeader = cursor.getString(0);
                    String header = LocaleStringHelper.cleanDisplayText(rawHeader);
                    if (header != null && !header.isEmpty() && !categoryList.contains(header)) {
                        categoryList.add(header);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Utility.getInstance().showLog(e);
        }

        // Fallback to default language if requested language has no entries
        if (categoryList.isEmpty() && !LocaleManager.LANGUAGE_DEFAULT.equals(languageCode)) {
            return getCategory(catName, LocaleManager.LANGUAGE_DEFAULT);
        }
        return categoryList;
    }

    /**
     * Get all visible aarti records, localized to the user's active language.
     *
     * @return list of localized AartiData objects
     */
    public ArrayList<AartiData> getAartiCategory() {
        return getAartiCategory(LocaleManager.getSavedLanguage(ctx));
    }

    public ArrayList<AartiData> getAartiCategory(String languageCode) {
        ArrayList<AartiData> aartiList = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();
        String query = "SELECT a.id, " +
                "COALESCE(t.name, a.name) AS name, " +
                "COALESCE(t.lyrics, a.lyrics) AS lyrics, " +
                "a.isHindi " +
                "FROM " + TABLE_AARTI + " a " +
                "LEFT JOIN " + TABLE_AARTI_TRANSLATION + " t " +
                "  ON a.id = t.aarti_id AND t.language_code = ? " +
                "WHERE a.isVisible = 1 " +
                "ORDER BY a.id ASC";
        try (Cursor cursor = database.rawQuery(query, new String[]{languageCode})) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    AartiData aartiData = new AartiData();
                    aartiData.setId(String.valueOf(cursor.getInt(0)));
                    aartiData.setName(LocaleStringHelper.cleanDisplayText(cursor.getString(1)));
                    aartiData.setAartiDesc(LocaleStringHelper.cleanDisplayText(cursor.getString(2)));
                    aartiData.setIsHindi(cursor.getInt(3));
                    aartiList.add(aartiData);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Utility.getInstance().showLog(e);
        }

        // Fallback to default language if empty
        if (aartiList.isEmpty() && !LocaleManager.LANGUAGE_DEFAULT.equals(languageCode)) {
            return getAartiCategory(LocaleManager.LANGUAGE_DEFAULT);
        }
        return aartiList;
    }

    /**
     * Get slok details for a given subcategory header, localized to the user's active language.
     *
     * @param subCatName subcategory header name (may be localized or original source header)
     * @return list of localized SlokList objects
     */
    public ArrayList<SlokList> getSlockDetail(String subCatName) {
        return getSlockDetail(subCatName, LocaleManager.getSavedLanguage(ctx));
    }

    public ArrayList<SlokList> getSlockDetail(String subCatName, String languageCode) {
        ArrayList<SlokList> categoryList = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();
        String cleanedHeader = LocaleStringHelper.cleanDisplayText(subCatName);
        String targetHeader = cleanedHeader != null ? cleanedHeader : (subCatName != null ? subCatName.trim() : "");
        String query = "SELECT m.ID, m.slok_category, " +
                "COALESCE(t.slok_header, m.slok_header) AS header, " +
                "COALESCE(t.slok_name, m.slok_name) AS slok_name, " +
                "COALESCE(t.slok, m.slok) AS slok " +
                "FROM " + TABLE_MAIN + " m " +
                "LEFT JOIN " + TABLE_MANTRAS_TRANSLATION + " t " +
                "  ON m.ID = t.mantra_id AND t.language_code = ? " +
                "WHERE m.ID IN (" +
                "  SELECT mantra_id FROM " + TABLE_MANTRAS_TRANSLATION + " " +
                "  WHERE slok_header = ? OR TRIM(REPLACE(slok_header, '.', '')) = ? " +
                "  UNION " +
                "  SELECT ID FROM " + TABLE_MAIN + " " +
                "  WHERE slok_header = ? OR TRIM(REPLACE(slok_header, '.', '')) = ? " +
                ") " +
                "ORDER BY m.ID ASC";
        try (Cursor cursor = database.rawQuery(query, new String[]{languageCode, targetHeader, targetHeader, targetHeader, targetHeader})) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    SlokList slokdata = new SlokList();
                    slokdata.setHeader(LocaleStringHelper.cleanDisplayText(cursor.getString(2)));
                    slokdata.setSlok_name(LocaleStringHelper.cleanDisplayText(cursor.getString(3)));
                    slokdata.setSlok(cursor.getString(4));
                    categoryList.add(slokdata);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Utility.getInstance().showLog(e);
        }

        // Fallback to default language if empty
        if (categoryList.isEmpty() && !LocaleManager.LANGUAGE_DEFAULT.equals(languageCode)) {
            return getSlockDetail(subCatName, LocaleManager.LANGUAGE_DEFAULT);
        }
        return categoryList;
    }

    public static class SearchResult implements java.io.Serializable {
        public int id;
        public String title;
        public String subtitle;
        public String categoryName;
        public int categoryIndex;
        public Object data;

        public SearchResult(int id, String title, String subtitle, String categoryName, int categoryIndex, Object data) {
            this.id = id;
            this.title = title;
            this.subtitle = subtitle;
            this.categoryName = categoryName;
            this.categoryIndex = categoryIndex;
            this.data = data;
        }
    }

    public AartiData getAartiById(int aartiId, String languageCode) {
        SQLiteDatabase database = getReadableDatabase();
        String query = "SELECT a.id, COALESCE(t.name, a.name), COALESCE(t.lyrics, a.lyrics), a.isHindi " +
                "FROM " + TABLE_AARTI + " a " +
                "LEFT JOIN " + TABLE_AARTI_TRANSLATION + " t ON a.id = t.aarti_id AND t.language_code = ? " +
                "WHERE a.id = ? LIMIT 1";
        try (Cursor cursor = database.rawQuery(query, new String[]{languageCode, String.valueOf(aartiId)})) {
            if (cursor != null && cursor.moveToFirst()) {
                AartiData aartiData = new AartiData();
                aartiData.setId(String.valueOf(cursor.getInt(0)));
                aartiData.setName(LocaleStringHelper.cleanDisplayText(cursor.getString(1)));
                aartiData.setAartiDesc(LocaleStringHelper.cleanDisplayText(cursor.getString(2)));
                aartiData.setIsHindi(cursor.getInt(3));
                return aartiData;
            }
        } catch (Exception e) {
            Utility.getInstance().showLog(e);
        }
        return null;
    }

    public ArrayList<SearchResult> searchContent(String rawQuery, String languageCode) {
        ArrayList<SearchResult> results = new ArrayList<>();
        if (rawQuery == null || rawQuery.trim().isEmpty()) {
            return results;
        }
        String pattern = "%" + rawQuery.trim() + "%";
        SQLiteDatabase database = getReadableDatabase();

        // 1. Search Aartis
        try {
            String aartiQuery = "SELECT a.id, COALESCE(t.name, a.name), COALESCE(t.lyrics, a.lyrics), a.isHindi " +
                    "FROM " + TABLE_AARTI + " a " +
                    "LEFT JOIN " + TABLE_AARTI_TRANSLATION + " t ON a.id = t.aarti_id AND t.language_code = ? " +
                    "WHERE a.isVisible = 1 AND (t.name LIKE ? OR t.lyrics LIKE ? OR a.name LIKE ? OR a.lyrics LIKE ?) " +
                    "LIMIT 20";
            try (Cursor c = database.rawQuery(aartiQuery, new String[]{languageCode, pattern, pattern, pattern, pattern})) {
                while (c != null && c.moveToNext()) {
                    int id = c.getInt(0);
                    String name = LocaleStringHelper.cleanDisplayText(c.getString(1));
                    String lyrics = LocaleStringHelper.cleanDisplayText(c.getString(2));
                    AartiData aartiData = new AartiData();
                    aartiData.setId(String.valueOf(id));
                    aartiData.setName(name);
                    aartiData.setAartiDesc(lyrics);
                    aartiData.setIsHindi(c.getInt(3));

                    String snippet = lyrics != null && lyrics.length() > 60 ? lyrics.substring(0, 60) + "..." : lyrics;
                    results.add(new SearchResult(id, name, snippet, "आरती", Utility.MAINCATEGORY.AARTI.ordinal(), aartiData));
                }
            }
        } catch (Exception e) {
            Utility.getInstance().showLog(e);
        }

        // 2. Search Mantras
        try {
            String mantraQuery = "SELECT m.ID, m.slok_category, COALESCE(t.slok_header, m.slok_header), " +
                    "COALESCE(t.slok_name, m.slok_name), COALESCE(t.slok, m.slok) " +
                    "FROM " + TABLE_MAIN + " m " +
                    "LEFT JOIN " + TABLE_MANTRAS_TRANSLATION + " t ON m.ID = t.mantra_id AND t.language_code = ? " +
                    "WHERE (t.slok_header LIKE ? OR t.slok_name LIKE ? OR t.slok LIKE ? OR m.slok_header LIKE ? OR m.slok_name LIKE ?) " +
                    "LIMIT 30";
            try (Cursor c = database.rawQuery(mantraQuery, new String[]{languageCode, pattern, pattern, pattern, pattern, pattern})) {
                while (c != null && c.moveToNext()) {
                    int id = c.getInt(0);
                    String catCode = c.getString(1);
                    String header = LocaleStringHelper.cleanDisplayText(c.getString(2));
                    String name = LocaleStringHelper.cleanDisplayText(c.getString(3));

                    int catIndex = Utility.MAINCATEGORY.AVAHANAM.ordinal();
                    String catDisplayName = "आवाहन";
                    if ("hom".equalsIgnoreCase(catCode)) {
                        catIndex = Utility.MAINCATEGORY.HOME.ordinal();
                        catDisplayName = "होम";
                    } else if ("rajopchar".equalsIgnoreCase(catCode)) {
                        catIndex = Utility.MAINCATEGORY.RAJOPACHARPUJA.ordinal();
                        catDisplayName = "राजोपचार पूजा";
                    }

                    results.add(new SearchResult(id, name != null && !name.isEmpty() ? name : header, header, catDisplayName, catIndex, header));
                }
            }
        } catch (Exception e) {
            Utility.getInstance().showLog(e);
        }

        return results;
    }
}

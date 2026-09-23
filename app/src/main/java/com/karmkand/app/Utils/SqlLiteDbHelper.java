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
    private static final int DATABASE_VERSION = 3;

    // Database Name
    private static final String DATABASE_NAME = "MantraVidhiDB";

    // Contacts table name
    private static final String TABLE_MAIN = "tbl_mantras";
    private static final String TABLE_AARTI = "tbl_aarti";

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

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * get all category from databse
     *
     * @param catName - category Name
     * @return
     */
    public List<String> getCategory(String catName) {
        ArrayList<String> categoryList = new ArrayList<String>();
        SQLiteDatabase database = getReadableDatabase();
        try (Cursor cursor = database.rawQuery("select slok_header from " + TABLE_MAIN + " where slok_category = ? group by slok_header", new String[]{catName})) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String header = cursor.getString(0);
                    if (!categoryList.contains(header)) {
                        categoryList.add(header);
                    }
                } while (cursor.moveToNext());
            }
        }
        return categoryList;
    }

    /**
     * get all aarti category from db
     *
     * @return
     */
    public ArrayList<AartiData> getAartiCategory() {
        ArrayList<AartiData> aartiList = new ArrayList<AartiData>();
        SQLiteDatabase database = getReadableDatabase();
        try (Cursor cursor = database.rawQuery("select name, lyrics, isHindi from " + TABLE_AARTI + " where isVisible = 1 ORDER BY isHindi ASC", null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    AartiData aartiData = new AartiData();
                    aartiData.setName(cursor.getString(0));
                    aartiData.setAartiDesc(cursor.getString(1));
                    aartiData.setIsHindi(cursor.getInt(2));
                    aartiList.add(aartiData);
                } while (cursor.moveToNext());
            }
        }
        return aartiList;
    }

    /**
     * get slock detail from database
     *
     * @param subCatName - category Name
     * @return
     */
    public ArrayList<SlokList> getSlockDetail(String subCatName) {
        ArrayList<SlokList> categoryList = new ArrayList<SlokList>();
        SQLiteDatabase database = getReadableDatabase();
        try (Cursor cursor = database.rawQuery("select * from " + TABLE_MAIN + " where slok_header = ?", new String[]{subCatName})) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    SlokList slokdata = new SlokList();
                    slokdata.setHeader(cursor.getString(2));
                    slokdata.setSlok_name(cursor.getString(3));
                    slokdata.setSlok(cursor.getString(4));
                    categoryList.add(slokdata);
                } while (cursor.moveToNext());
            }
        }
        return categoryList;
    }
}

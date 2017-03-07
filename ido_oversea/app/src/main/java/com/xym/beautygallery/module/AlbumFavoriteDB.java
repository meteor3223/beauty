package com.xym.beautygallery.module;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.xym.beautygallery.base.FeatureConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 7/12/16.
 */
public class AlbumFavoriteDB extends SQLiteOpenHelper {
    private final static String Tag = "AlbumFavoriteDB";
    private static final boolean DEBUG = FeatureConfig.DEBUG && false;
    private final static String DB_NAME = "album_favorite_db";
    private final static int DB_VERSION = 1;
    private final static String FAVORITE_DATA_TABLE_NAME = "album_favorite_data";
    private final static String COLUMN_ID = "id";
    private final static String COLUMN_ALBUM_NAME = "album_name";
    private final static String COLUMN_ALBUM_ADDRESS = "album_address";
    private final static String COLUMN_ALBUM_THUMB = "album_thumb";
    private final static String COLUMN_ALBUM_PICS = "album_pics";
    private final static String COLUMN_ALBUM_WIDTH = "album_width";
    private final static String COLUMN_ALBUM_HEIGHT = "album_height";
    private final static String COLUMN_USER_LOVE = "love";
    private final static String COLUMN_LOVE_TIME = "time";

    private final static String CREATE_FAVORITE_DATA = "CREATE TABLE IF NOT EXISTS " + FAVORITE_DATA_TABLE_NAME + "(" + COLUMN_ID
            + " integer primary key, "
            + COLUMN_ALBUM_PICS + " text, "
            + COLUMN_USER_LOVE + " int, "
            + COLUMN_ALBUM_WIDTH + " int, "
            + COLUMN_ALBUM_HEIGHT + " int, "
            + COLUMN_LOVE_TIME + " long, "
            + COLUMN_ALBUM_NAME + " text, "
            + COLUMN_ALBUM_ADDRESS + " text, "
            + COLUMN_ALBUM_THUMB + " text " + ")";

    private static AlbumFavoriteDB mInstance;
    private SQLiteDatabase mDb;
    private Context mContext;

    private AlbumFavoriteDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context.getApplicationContext();
    }

    public static AlbumFavoriteDB getInstance(Context ctx) {
        if (mInstance == null) {
            synchronized (AlbumFavoriteDB.class) {
                if (mInstance == null) {
                    mInstance = new AlbumFavoriteDB(ctx);
                }
            }
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_FAVORITE_DATA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void openReadableDb() {
        if (mDb == null || !mDb.isOpen()) {
            mDb = this.getWritableDatabase();
        }
    }

    public void addAndUpdateFavoriteData(String album_name, String album_address, String album_thumb, String album_pics, int is_love, int album_width, int album_height, long love_time) {
        if (album_address == null) return;
        openReadableDb();
        Cursor cursor = null;
        try {
            cursor = mDb.rawQuery("SELECT * FROM " + FAVORITE_DATA_TABLE_NAME + " WHERE " + COLUMN_ALBUM_ADDRESS + " = ?",
                    new String[]{album_address});
            ContentValues values = new ContentValues();
            values.put(COLUMN_ALBUM_NAME, album_name);
            values.put(COLUMN_ALBUM_ADDRESS, album_address);
            values.put(COLUMN_ALBUM_THUMB, album_thumb);
            values.put(COLUMN_ALBUM_PICS, album_pics);
            values.put(COLUMN_USER_LOVE, is_love);
            values.put(COLUMN_LOVE_TIME, love_time);
            values.put(COLUMN_ALBUM_WIDTH, album_width);
            values.put(COLUMN_ALBUM_HEIGHT, album_height);
            if (cursor != null && cursor.getCount() > 0) {
                mDb.update(FAVORITE_DATA_TABLE_NAME, values, COLUMN_ALBUM_ADDRESS + " = ?", new String[]{album_address});
            } else {
                mDb.insert(FAVORITE_DATA_TABLE_NAME, null, values);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void addFavoriteData(String album_name, String album_address, String album_thumb, String album_pics, int is_love, int album_width, int album_height, long love_time) {
        if (album_address == null) return;
        openReadableDb();
        Cursor cursor = null;
        try {
            cursor = mDb.rawQuery("SELECT * FROM " + FAVORITE_DATA_TABLE_NAME + " WHERE " + COLUMN_ALBUM_ADDRESS + " = ?",
                    new String[]{album_address});
            if (cursor != null && cursor.getCount() > 0) {

            } else {
                ContentValues values = new ContentValues();
                values.put(COLUMN_ALBUM_NAME, album_name);
                values.put(COLUMN_ALBUM_ADDRESS, album_address);
                values.put(COLUMN_ALBUM_THUMB, album_thumb);
                values.put(COLUMN_ALBUM_PICS, album_pics);
                values.put(COLUMN_USER_LOVE, is_love);
                values.put(COLUMN_LOVE_TIME, love_time);
                values.put(COLUMN_ALBUM_WIDTH, album_width);
                values.put(COLUMN_ALBUM_HEIGHT, album_height);
                mDb.insert(FAVORITE_DATA_TABLE_NAME, null, values);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void updateFavoriteData(String album_name, String album_address, String album_thumb, String album_pics, int is_love, int album_width, int album_height, long love_time) {
        if (album_address == null) return;
        openReadableDb();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ALBUM_NAME, album_name);
        values.put(COLUMN_ALBUM_ADDRESS, album_address);
        values.put(COLUMN_ALBUM_THUMB, album_thumb);
        values.put(COLUMN_ALBUM_PICS, album_pics);
        values.put(COLUMN_USER_LOVE, is_love);
        values.put(COLUMN_LOVE_TIME, love_time);
        values.put(COLUMN_ALBUM_WIDTH, album_width);
        values.put(COLUMN_ALBUM_HEIGHT, album_height);
        mDb.update(FAVORITE_DATA_TABLE_NAME, values, COLUMN_ALBUM_ADDRESS + " = ?", new String[]{album_address});
    }


    public ArrayList<AlbumInfo> queryFavoriteData() {
        openReadableDb();
        ArrayList<AlbumInfo> result = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = mDb.rawQuery("SELECT * FROM " + FAVORITE_DATA_TABLE_NAME + " WHERE " + COLUMN_USER_LOVE + " = ?",
                    new String[]{"1"});
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String album_name = cursor.getString(cursor.getColumnIndex(COLUMN_ALBUM_NAME));
                    String album_address = cursor.getString(cursor.getColumnIndex(COLUMN_ALBUM_ADDRESS));
                    String album_thumb = cursor.getString(cursor.getColumnIndex(COLUMN_ALBUM_THUMB));
                    String album_pics = cursor.getString(cursor.getColumnIndex(COLUMN_ALBUM_PICS));
                    int isLove = cursor.getInt(cursor.getColumnIndex(COLUMN_USER_LOVE));
                    int album_width = cursor.getInt(cursor.getColumnIndex(COLUMN_ALBUM_WIDTH));
                    int album_height = cursor.getInt(cursor.getColumnIndex(COLUMN_ALBUM_HEIGHT));
                    long loveTime = cursor.getLong(cursor.getColumnIndex(COLUMN_LOVE_TIME));
                    AlbumInfo albumInfo = new AlbumInfo();
                    albumInfo.album_name = album_name;
                    albumInfo.album_address = album_address;
                    albumInfo.album_thumb = album_thumb;
                    albumInfo.album_pics = album_pics;
                    albumInfo.album_width = album_width;
                    albumInfo.album_height = album_height;
                    albumInfo.love_time = loveTime;
                    if (isLove > 0) {
                        albumInfo.is_love = 1;
                        result.add(albumInfo);
                    } else {
                        albumInfo.is_love = 0;
                    }
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }
}

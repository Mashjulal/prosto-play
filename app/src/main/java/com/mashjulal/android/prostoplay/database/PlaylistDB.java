package com.mashjulal.android.prostoplay.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.mashjulal.android.prostoplay.database.PlaylistDBSchema.PlaylistTable.Columns.ARTIST;
import static com.mashjulal.android.prostoplay.database.PlaylistDBSchema.PlaylistTable.Columns.BITRATE;
import static com.mashjulal.android.prostoplay.database.PlaylistDBSchema.PlaylistTable.Columns.LENGTH;
import static com.mashjulal.android.prostoplay.database.PlaylistDBSchema.PlaylistTable.Columns.PATH;
import static com.mashjulal.android.prostoplay.database.PlaylistDBSchema.PlaylistTable.Columns.SIZE;
import static com.mashjulal.android.prostoplay.database.PlaylistDBSchema.PlaylistTable.Columns.TRACK;
import static com.mashjulal.android.prostoplay.database.PlaylistDBSchema.PlaylistTable.Columns.TRACK_ID;
import static com.mashjulal.android.prostoplay.database.PlaylistDBSchema.PlaylistTable.TABLE_NAME;

public class PlaylistDB {

    private static final String DATABASE_NAME = "playlistBase.db";
    private static final int VERSION = 1;

    private static final String DB_CREATE =
            "CREATE TABLE " + TABLE_NAME + "(" +
                    TRACK_ID + " TEXT PRIMARY KEY, " +
                    TRACK + " TEXT, " +
                    ARTIST + " TEXT, " +
                    LENGTH + " INTEGER, " +
                    BITRATE + " TEXT, " +
                    SIZE + " INTEGER, " +
                    PATH + " TEXT);";

    private Context context;

    private DBHelper dbHelper;
    private SQLiteDatabase db;

    public PlaylistDB(Context c){
        context = c;
    }

    public void open(){
        dbHelper = new DBHelper(context, DATABASE_NAME, null, VERSION);
        db = dbHelper.getWritableDatabase();
    }

    public void close(){
        if (dbHelper != null)
            dbHelper.close();
    }

    public Cursor getAllData(){
        return db.query(TABLE_NAME, null, null, null, null, null, null);
    }

    public void addData(ContentValues cv){
        db.insert(TABLE_NAME, null, cv);
    }

    public void deleteData(String trackId){
        db.delete(TABLE_NAME, TRACK_ID + " = '" + trackId + "'", null);
    }

    public Cursor getData(String whereClause, String[] whereArgs){
        return db.query(TABLE_NAME, null, whereClause, whereArgs, null, null, null);
    }

    private class DBHelper extends SQLiteOpenHelper {
        DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}

package com.mashjulal.android.prostoplay;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.mashjulal.android.prostoplay.database.PlaylistDB;

import java.util.ArrayList;
import java.util.List;

import static com.mashjulal.android.prostoplay.database.PlaylistDBSchema.PlaylistTable.Columns.ARTIST;
import static com.mashjulal.android.prostoplay.database.PlaylistDBSchema.PlaylistTable.Columns.BITRATE;
import static com.mashjulal.android.prostoplay.database.PlaylistDBSchema.PlaylistTable.Columns.LENGTH;
import static com.mashjulal.android.prostoplay.database.PlaylistDBSchema.PlaylistTable.Columns.PATH;
import static com.mashjulal.android.prostoplay.database.PlaylistDBSchema.PlaylistTable.Columns.SIZE;
import static com.mashjulal.android.prostoplay.database.PlaylistDBSchema.PlaylistTable.Columns.TRACK;
import static com.mashjulal.android.prostoplay.database.PlaylistDBSchema.PlaylistTable.Columns.TRACK_ID;

class TrackLab {
    private static TrackLab ourInstance;
    private PlaylistDB db;

    static TrackLab getInstance(Context context) {
        if (ourInstance == null)
            ourInstance = new TrackLab(context);
        return ourInstance;
    }

    private TrackLab(Context context) {
        db = new PlaylistDB(context);
    }

    List<Song> getSongs(){
        List<Song> songs = new ArrayList<>();

        db.open();
        Cursor cursor = db.getAllData();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Song song = getSong(cursor);
                songs.add(song);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();

        return songs;
    }

    void addSong(Song song){
        db.open();
        ContentValues cv = getContentValues(song);
        db.addData(cv);
        db.close();
    }

    Song getSong(String trackId){
        db.open();
        Cursor cursor = db.getData(TRACK_ID + " = ?", new String[]{trackId});
        cursor.moveToFirst();
        Song song = getSong(cursor);
        cursor.close();
        db.close();

        return song;
    }

    void removeSong(String id){
        db.open();
        db.deleteData(id);
        db.close();
    }

    private ContentValues getContentValues(Song track){
        ContentValues cv = new ContentValues();
        cv.put(TRACK_ID, track.getId());
        cv.put(TRACK, track.getName());
        cv.put(ARTIST, track.getArtist());
        cv.put(LENGTH, track.getLength());
        cv.put(BITRATE, track.getBitrate());
        cv.put(SIZE, track.getSize());
        cv.put(PATH, track.getPath());

        return cv;
    }

    private Song getSong(Cursor cursor){
        String trackId = cursor.getString(cursor.getColumnIndex(TRACK_ID));
        String track = cursor.getString(cursor.getColumnIndex(TRACK));
        String artist = cursor.getString(cursor.getColumnIndex(ARTIST));
        int length = cursor.getInt(cursor.getColumnIndex(LENGTH));
        String bitrate = cursor.getString(cursor.getColumnIndex(BITRATE));
        int size = cursor.getInt(cursor.getColumnIndex(SIZE));
        String path = cursor.getString(cursor.getColumnIndex(PATH));

        return new Song(trackId, track, artist, length, bitrate, size, path);
    }

    boolean hasSong(Song song){
        String id = song.getId();

        db.open();
        Cursor cursor = db.getData(TRACK_ID + " = ?", new String[]{id});

        if (cursor.getCount() <= 0){
            cursor.close();
            db.close();
            return false;
        }

        cursor.close();
        db.close();
        return true;
    }
}

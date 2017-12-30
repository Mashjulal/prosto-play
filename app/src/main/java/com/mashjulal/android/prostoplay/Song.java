package com.mashjulal.android.prostoplay;


import com.mashjulal.android.prostoplay.prosto_pleer.Track;

import java.io.Serializable;

class Song implements Serializable {

    private Track track;
    private String path;

    Song(Track track) {
        this.track = track;
    }

    Song(Track track, String path) {
        this.track = track;
        this.path = path;
    }

    Song(String trackId, String trackName, String artist, int length, String bitrate, int size, String path) {
        this.track = new Track(trackId, artist, trackName, length, bitrate, size);
        this.path = path;
    }

    public String getId() {
        return track.getId();
    }

    public String getArtist() {
        return track.getArtist();
    }

    public String getName() {
        return track.getName();
    }

    public int getLength() {
        return track.getLength();
    }

    public String getBitrate() {
        return track.getBitrate();
    }

    public int getSize() {
        return track.getSize();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}

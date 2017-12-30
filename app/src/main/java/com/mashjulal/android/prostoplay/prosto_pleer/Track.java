package com.mashjulal.android.prostoplay.prosto_pleer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


public class Track implements Serializable {
    @SerializedName(value = "track_id", alternate = "id")
    @Expose
    private String id;
    @SerializedName("artist")
    @Expose
    private String artist;
    @SerializedName("track")
    @Expose
    private String name;
    @SerializedName("lenght")
    @Expose
    private int length;
    @SerializedName("bitrate")
    @Expose
    private String bitrate;
    @SerializedName("size")
    @Expose
    private int size;

    public Track(String id, String artist, String name, int length, String bitrate, int size) {
        this.id = id;
        this.artist = artist;
        this.name = name;
        this.length = length;
        this.bitrate = bitrate;
        this.size = size;
    }

    public String getId() {
        return id;
    }

    public String getArtist() {
        return artist;
    }

    public String getName() {
        return name;
    }

    public int getLength() {
        return length;
    }

    public String getBitrate() {
        return bitrate;
    }

    public int getSize() {
        return size;
    }
}

package com.mashjulal.android.prostoplay.prosto_pleer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Created by Master on 05.07.2017.
 */

public class TrackListModel {

    @SerializedName("success")
    @Expose
    private Boolean success;

    @SerializedName("count")
    @Expose
    private int count;

    @SerializedName("tracks")
    @Expose
    private Map<String, Track> trackMap;

    public Boolean isSuccess() {
        return success;
    }

    public int getCount() {
        return count;
    }

    public Map<String, Track> getTrackMap() {
        return trackMap;
    }
}

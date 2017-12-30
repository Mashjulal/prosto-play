package com.mashjulal.android.prostoplay.prosto_pleer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Master on 05.07.2017.
 */

public class TrackInfoModel {

    @SerializedName("success")
    @Expose
    private Boolean success;

    @SerializedName("data")
    @Expose
    private Track track;

    public Boolean isSuccess() {
        return success;
    }

    public Track getTrack() {
        return track;
    }
}

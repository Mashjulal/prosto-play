package com.mashjulal.android.prostoplay.prosto_pleer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Master on 05.07.2017.
 */

public class DownloadLinkModel {

    @SerializedName("success")
    @Expose
    private Boolean success;

    @SerializedName("url")
    @Expose
    private String url;

    public Boolean getSuccess() {
        return success;
    }

    public String getUrl() {
        return url;
    }
}

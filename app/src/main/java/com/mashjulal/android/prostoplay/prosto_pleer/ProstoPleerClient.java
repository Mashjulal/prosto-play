package com.mashjulal.android.prostoplay.prosto_pleer;

import android.content.Context;
import android.content.res.Resources;

import com.mashjulal.android.prostoplay.R;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;


public class ProstoPleerClient {
    private static final String GRANT_TYPE = "client_credentials";
    private static final String METHOD_TRACKS_SEARCH = "tracks_search";
    private static final String METHOD_TRACKS_GET_INFO = "tracks_get_info";
    private static final String METHOD_TRACKS_GET_DOWNLOAD_LINK = "tracks_get_download_link";

    private static final String REASON_SAVE = "save";

    private static String client_id;
    private static String client_secret;
    private static String username;
    private static String password;
    private static String accessToken;

    private static ProstoPleerAPI mProstoPleerAPI;
    private static ProstoPleerClient instance;

    public static ProstoPleerClient getInstance(Context context) {
        if (instance == null) {
            instance = new ProstoPleerClient();
            createApi(context);
        }
        return instance;
    }

    private ProstoPleerClient() {
    }

    public static void setAccessToken(String accessToken) {
        ProstoPleerClient.accessToken = accessToken;
    }

    private static void createApi(Context context){
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.pleer.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        mProstoPleerAPI = retrofit.create(ProstoPleerAPI.class);
        Resources res = context.getResources();
        username = res.getString(R.string.auth_name);
        password = res.getString(R.string.auth_password);
        client_id = res.getString(R.string.auth_client_id);
        client_secret = res.getString(R.string.auth_client_secret);
    }

    public Call<AccessTokenModel> authorize(){
        return mProstoPleerAPI.authorize(GRANT_TYPE, client_id, client_secret, username, password);
    }

    public Call<TrackListModel> searchTracks(String query, int page, int resultOnPage){
        return mProstoPleerAPI.searchTracks(
                METHOD_TRACKS_SEARCH, accessToken, query, String.valueOf(page), String.valueOf(resultOnPage));
    }

    public Call<TrackInfoModel> getTrackInfo(String trackId){
        return mProstoPleerAPI
                .getTrackInfo(METHOD_TRACKS_GET_INFO, accessToken, trackId);
    }

    public Call<DownloadLinkModel> getDownloadLink(String trackId){
        return mProstoPleerAPI.getDownloadLink(
                METHOD_TRACKS_GET_DOWNLOAD_LINK,
                accessToken,
                trackId,
                REASON_SAVE);
    }

    public boolean hasAccessToken() {
        return accessToken != null;
    }

    private interface ProstoPleerAPI {

        @FormUrlEncoded
        @POST("/token.php")
        Call<AccessTokenModel> authorize(
                @Field("grant_type") String grantType,
                @Field("client_id") String clientId,
                @Field("client_secret") String clientSecret,
                @Field("user_username") String username,
                @Field("user_password") String userPassword);

        @FormUrlEncoded
        @POST("/index.php")
        Call<TrackListModel> searchTracks(
                @Field("method") String method,
                @Field("access_token") String accessToken,
                @Field("query") String query,
                @Field("page") String page,
                @Field("result_on_page") String resultOnPage);

        @FormUrlEncoded
        @POST("/index.php")
        Call<TrackInfoModel> getTrackInfo(
                @Field("method") String method,
                @Field("access_token") String accessToken,
                @Field("track_id") String trackId);

        @FormUrlEncoded
        @POST("/index.php")
        Call<DownloadLinkModel> getDownloadLink(
                @Field("method") String method,
                @Field("access_token") String accessToken,
                @Field("track_id") String trackId,
                @Field("reason") String reason);

    }
}

package com.mashjulal.android.prostoplay;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.mashjulal.android.prostoplay.prosto_pleer.DownloadLinkModel;
import com.mashjulal.android.prostoplay.prosto_pleer.ProstoPleerClient;
import com.squareup.otto.Subscribe;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mashjulal.android.prostoplay.MusicPlayerFragment.CODE_NEXT;
import static com.mashjulal.android.prostoplay.MusicPlayerFragment.CODE_PAUSE;
import static com.mashjulal.android.prostoplay.MusicPlayerFragment.CODE_PREV;
import static com.mashjulal.android.prostoplay.MusicPlayerFragment.CODE_RESUME;

public class MusicPlayerService extends Service {

    private static final String TAG = MusicPlayerService.class.getSimpleName();

    private Playlist playlist = new Playlist();
    private MediaPlayer mPlayer;
    private Context mContext;
    private static ChangeSongListener mSongListener;
    private boolean isPreparing = false;
    private boolean isLoading = false;

    MusicPlayerBinder binder = new MusicPlayerBinder();

    public MusicPlayerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ProstoPlayApplication.getBus().register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mContext = getApplicationContext();
        if (intent != null) {
            if (intent.hasExtra("SENDER_NAME") &&
                    intent.getStringExtra("SENDER_NAME").equals(ProstoPlayNotification.CLASS_NAME)) {
                if (intent.getBooleanExtra("pause", false))
                    pause();
                else
                    playFromPosition(mPlayer.getCurrentPosition());
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Subscribe
    public void getMessage(String message) {
        try {
            Log.d(TAG + "1", (message != null) ? message : "null");
            mPlayer.setDataSource(message);
            mPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void getMessage(MusicPlayerFragment.ButtonClick buttonClick) {
        int messageCode = buttonClick.getButtonState();
        switch (messageCode) {
            case CODE_RESUME: // Resume Button
                resume();
                break;
            case CODE_PAUSE: // Pause Button
                pause();
                break;
            case CODE_NEXT: // Next Button
                goToNextSong();
                break;
            case CODE_PREV: // Prev Button
                goToPrevSong();
                break;
        }
    }

    int getDuration(){
        return mPlayer.getDuration();
    }

    void prepare(){
        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mSongListener.onChange(playlist.getCurrentSong());
                play();
                isPreparing = false;
            }
        });
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                goToNextSong();
            }
        });
        isPreparing = true;
        if (playlist.isPlaying())
            stop();
        final Song song = playlist.getCurrentSong();
        String songPath = song.getPath();
        if (songPath == null) {
            ProstoPleerClient client = ProstoPleerClient.getInstance(mContext);
            client.getDownloadLink(song.getId()).enqueue(new Callback<DownloadLinkModel>() {
                @Override
                public void onResponse(Call<DownloadLinkModel> call, Response<DownloadLinkModel> response) {
                    DownloadLinkModel model = response.body();
                    if (model != null) {
                        String songPath = model.getUrl();
                        song.setPath(songPath);
                        ProstoPlayApplication.getBus().post(songPath);
                    }
                }

                @Override
                public void onFailure(Call<DownloadLinkModel> call, Throwable t) {
                    Log.e(TAG, call.toString());
                }
            });
            isLoading = true;
        } else {
            ProstoPlayApplication.getBus().post(songPath);
        }
    }

    void play() {
        mPlayer.start();
        playlist.resume();
        Song song = playlist.getCurrentSong();
        ProstoPlayNotification.notify(getApplicationContext(), song.getName(), song.getArtist(), "pause");
    }

    void pause(){
        mPlayer.pause();
        playlist.pause();
        Song song = playlist.getCurrentSong();
        ProstoPlayNotification.notify(getApplicationContext(), song.getName(), song.getArtist(), "play");
    }

    void resume(){
        mPlayer.seekTo(mPlayer.getCurrentPosition());
        mPlayer.start();
        playlist.resume();
        Song song = playlist.getCurrentSong();
        ProstoPlayNotification.notify(getApplicationContext(), song.getName(), song.getArtist(), "pause");
    }

    void goToNextSong() {
        if (!playlist.isStopped())
            stop();
        playlist.nextSong();
        prepare();
    }

    void goToPrevSong() {
        if (!playlist.isStopped())
            stop();
        playlist.nextSong();
        prepare();
    }

    void playFromPosition(int position){
        mPlayer.seekTo(position);
        mPlayer.start();
        playlist.resume();
        Song song = playlist.getCurrentSong();
        ProstoPlayNotification.notify(getApplicationContext(), song.getName(), song.getArtist(), "pause");
    }

    void stop(){
        mPlayer.stop();
        mPlayer.reset();
        playlist.stop();
    }

    Playlist getPlaylist(){
        return playlist;
    }

    MediaPlayer getPlayer(){
        return mPlayer;
    }

    int getPosition(){
        return mPlayer.getCurrentPosition();
    }

    void setOnSongChangeListener(ChangeSongListener listener) {
        mSongListener = listener;
    }

    class MusicPlayerBinder extends Binder {
        MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }

    interface ChangeSongListener {
        void onChange(Song newSong);
    }
}

package com.mashjulal.android.prostoplay;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Master on 09.06.2017.
 */

public class Playlist {

    private List<Song> songs = new ArrayList<>();
    private int currentSongIndex;
    private Song currentSong;

    private boolean isPlaying = false;
    private boolean isPaused = false;

    void setSongs(List<Song> s){
        songs = s;
    }

    void setCurrentSong(Song s){
        currentSong = s;
        String id = currentSong.getId();

        for (int i = 0; i < songs.size(); i++){
            if (songs.get(i).getId().equals(id)) {
                currentSongIndex = i;
                break;
            }
        }
    }

    void nextSong(){
        if (currentSongIndex + 1 < songs.size())
            currentSongIndex++;
        else
            currentSongIndex = 0;
        currentSong = songs.get(currentSongIndex);
    }

    void prevSong(){
        if (currentSongIndex > 0)
            currentSongIndex--;
        else
            currentSongIndex = songs.size();
        currentSong = songs.get(currentSongIndex);
    }

    Song getCurrentSong(){
        return currentSong;
    }

    int getDuration(){
        return currentSong.getLength() * 1000;
    }

    void pause(){
        isPaused = true;
        isPlaying = false;
    }

    void resume(){
        isPlaying = true;
        isPaused = false;
    }

    void stop(){
        isPaused = false;
        isPlaying = false;
    }

    boolean isPlaying(){
        return isPlaying;
    }

    boolean isPaused(){
        return isPaused;
    }

    boolean isStopped(){
        return !(isPaused || isPlaying);
    }

    List<Song> getSongs(){
        return songs;
    }
}

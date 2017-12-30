package com.mashjulal.android.prostoplay;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;


public class MusicPlayerFragment extends android.app.Fragment {

    public static final int CODE_RESUME = 0;
    public static final int CODE_PAUSE = 1;
    public static final int CODE_NEXT = 2;
    public static final int CODE_PREV = 3;

    private static final String PLAY = "play";
    private static final String PAUSE = "pause";

    private Handler mHandler = new Handler();

    private View v;
    private ImageButton btnPlay;
    private ImageButton btnPrev;
    private ImageButton btnNext;
    private TextView tvSongName;
    private TextView tvArtist;
    private SeekBar seekBar;
    private MusicPlayerService mPPService;
    private boolean isVisible = false;

    private View.OnClickListener onPlay = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ProstoPlayApplication.getBus().post(new ButtonClick(CODE_RESUME));
            changePlayButtonStateTo(PAUSE);
        }
    };
    private View.OnClickListener onPause = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ProstoPlayApplication.getBus().post(new ButtonClick(CODE_PAUSE));
            changePlayButtonStateTo(PLAY);
        }
    };
    private View.OnClickListener onNext = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ProstoPlayApplication.getBus().post(new ButtonClick(CODE_NEXT));
        }
    };

    private View.OnClickListener onPrev = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ProstoPlayApplication.getBus().post(new ButtonClick(CODE_PREV));
        }
    };

    public MusicPlayerFragment() {
        // Required empty public constructor
    }

    public static MusicPlayerFragment newInstance() {
        return new MusicPlayerFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ProstoPlayApplication.getBus().register(this);
        mPPService = ((MainActivity) getActivity()).getMusicPlayerService();
        mPPService.setOnSongChangeListener(new MusicPlayerService.ChangeSongListener() {
            @Override
            public void onChange(Song newSong) {
                updateTextFields();
                seekBar.setProgress(0);
                seekBar.setMax(mPPService.getDuration() / 1000);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(mPPService.getPlayer() != null && mPPService.getPlaylist().isPlaying())
                            seekBar.setProgress(mPPService.getPosition() / 1000);
                        mHandler.postDelayed(this, 1000);
                    }
                });
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if(mPPService.getPlayer() != null && fromUser){
                            mPPService.playFromPosition(progress * 1000);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });
                btnNext.setOnClickListener(onNext);
                btnPrev.setOnClickListener(onPrev);
                changePlayButtonStateTo(PAUSE);
                if (!isVisible)
                    setVisible(true);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_music_player, container, false);

        seekBar = (SeekBar) v.findViewById(R.id.fragment_music_player_seekBar);

        btnPlay = (ImageButton) v.findViewById(R.id.fragment_music_player_btn_play);
        btnPrev = (ImageButton) v.findViewById(R.id.fragment_music_player_btn_prev);
        btnNext = (ImageButton) v.findViewById(R.id.fragment_music_player_btn_next);

        tvSongName = (TextView) v.findViewById(R.id.fragment_music_player_tv_song_name);
        tvArtist = (TextView) v.findViewById(R.id.fragment_music_player_tv_artist);

        setVisible(false);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        Song song = mPPService.getPlaylist().getCurrentSong();
        if (song != null) {
            updateTextFields();
            changePlayButtonStateTo(PLAY);
            setVisible(true);
        }
        if (mPPService.getPlaylist().isPlaying()) {
            changePlayButtonStateTo(PAUSE);
        }
    }

    private void updateTextFields(){
        Song song = mPPService.getPlaylist().getCurrentSong();
        tvSongName.setText(song.getName());
        tvArtist.setText(song.getArtist());
    }

    void setVisible(boolean visible){
        isVisible = visible;
        v.setVisibility((isVisible) ? View.VISIBLE : View.GONE);
    }

    void changePlayButtonStateTo(String state){
        btnPlay.setBackgroundResource(0);
        switch (state){
            case PLAY:
                btnPlay.setImageResource(R.drawable.ic_action_play);
                btnPlay.setOnClickListener(onPlay);
                break;
            case PAUSE:
                btnPlay.setImageResource(R.drawable.ic_action_pause);
                btnPlay.setOnClickListener(onPause);
                break;
        }
    }

    class ButtonClick {
        private int mButtonState;

        ButtonClick(int mButtonState) {
            this.mButtonState = mButtonState;
        }

        int getButtonState() {
            return mButtonState;
        }
    }
}

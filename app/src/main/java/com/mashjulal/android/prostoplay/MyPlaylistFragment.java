package com.mashjulal.android.prostoplay;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mashjulal.android.prostoplay.prosto_pleer.DownloadLinkModel;
import com.mashjulal.android.prostoplay.prosto_pleer.ProstoPleerClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MyPlaylistFragment extends android.app.Fragment {

    private RecyclerView rvSongs;
    private TextView tvNoSongs;

    private TrackLab tl;
    private Context mContext;
    private SongItemRecyclerViewAdapter mSongAdapter;
    private List<Song> mSongs = new ArrayList<>();
    private MusicPlayerService mPlayerService;

    public MyPlaylistFragment() {
        // Required empty public constructor
    }

    public static android.app.Fragment newInstance() {
        return new MyPlaylistFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPlayerService = ((MainActivity)getActivity()).getMusicPlayerService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = container.getContext();
        View view = inflater.inflate(R.layout.fragment_my_playlist, container, false);
        rvSongs = (RecyclerView) view.findViewById(R.id.fragment_my_playlist_rv_songs);
        tvNoSongs = (TextView) view.findViewById(R.id.fragment_my_playlist_tv_empty);
        tl = TrackLab.getInstance(mContext);
        setRecyclerView();
        return view;
    }

    @Override
    public void onStart() {
        populateList();
        super.onStart();
    }

    private void setRecyclerView() {
        mSongAdapter = new SongItemRecyclerViewAdapter(mSongs, R.layout.fragment_my_playlist);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        rvSongs.setLayoutManager(layoutManager);
        rvSongs.addItemDecoration(
                new DividerItemDecoration(mContext, layoutManager.getOrientation()));
        rvSongs.setAdapter(mSongAdapter);
        mSongAdapter.setOnClickListener(new SongItemRecyclerViewAdapter.OnClickListener() {
            @Override
            public void onViewClick(int position) {
                Song song = mSongs.get(position);
                mPlayerService.getPlaylist().setCurrentSong(song);
                if (!mSongs.equals(mPlayerService.getPlaylist().getSongs()))
                    mPlayerService.getPlaylist().setSongs(mSongs);
                mPlayerService.prepare();
            }

            @Override
            public void onButtonAddClick(int position) {
                addToPlaylist(position);
            }

            @Override
            public void onButtonDownloadClick(int position) {
                downloadSong(position);
            }

            @Override
            public void onButtonDeleteClick(int position) {
                removeFromPlaylist(position);
            }
        });
    }

    private void populateList(){
        mSongs.clear();
        mSongs.addAll(tl.getSongs());
        if (mSongs.size() == 0){
            rvSongs.setVisibility(View.GONE);
            tvNoSongs.setVisibility(View.VISIBLE);
        }
        else {
            rvSongs.setVisibility(View.VISIBLE);
            tvNoSongs.setVisibility(View.GONE);
        }
        mSongAdapter.notifyDataSetChanged();
    }

    private void addToPlaylist(int id) {
        final Song song = mSongs.get(id);
        ProstoPleerClient.getInstance(mContext)
                .getDownloadLink(String.valueOf(id)).enqueue(new Callback<DownloadLinkModel>() {
            @Override
            public void onResponse(Call<DownloadLinkModel> call, Response<DownloadLinkModel> response) {
                DownloadLinkModel model = response.body();
                if (model != null) {
                    song.setPath(model.getUrl());
                    tl.addSong(song);
                    Toast.makeText(
                            mContext,
                            String.format("Song \"%s\" was added to your playlist", song.getName()),
                            Toast.LENGTH_SHORT)
                            .show();
                    populateList();
                }
            }

            @Override
            public void onFailure(Call<DownloadLinkModel> call, Throwable t) {
                Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void removeFromPlaylist(int position) {
        final Song song = mSongs.get(position);
        tl.removeSong(song.getId());
        populateList();
    }

    private void downloadSong(int position) {
        final Song song = mSongs.get(position);
        ProstoPleerClient.getInstance(mContext).getDownloadLink(song.getId()).enqueue(new Callback<DownloadLinkModel>() {
            @Override
            public void onResponse(Call<DownloadLinkModel> call, Response<DownloadLinkModel> response) {
                DownloadLinkModel model = response.body();
                if (model != null) {
                    DownloadManager dm = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(model.getUrl()));
                    String fileName = String.format("%s - %s [pleer.net].mp3", song.getArtist(), song.getName());
                    request.setTitle(fileName);
                    request.setDescription(model.getUrl());
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                    dm.enqueue(request);
                }
            }

            @Override
            public void onFailure(Call<DownloadLinkModel> call, Throwable t) {
                Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

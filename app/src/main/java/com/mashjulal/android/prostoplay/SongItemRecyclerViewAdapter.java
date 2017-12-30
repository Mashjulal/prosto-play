package com.mashjulal.android.prostoplay;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

/**
 * Created by Master on 05.07.2017.
 */

public class SongItemRecyclerViewAdapter extends RecyclerView.Adapter<SongItemRecyclerViewAdapter.ViewHolder> {

    private int mFragmentId;
    private List<Song> mSongs;
    private OnClickListener mOnClickListener;

    interface OnClickListener {
        void onViewClick(int position);
        void onButtonAddClick(int position);
        void onButtonDownloadClick(int position);
        void onButtonDeleteClick(int position);
    }

    SongItemRecyclerViewAdapter(List<Song> songs, int fragmentId) {
        mSongs = songs;
        mFragmentId = fragmentId;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final int pos = position;
        final Song song = mSongs.get(position);
        int time = song.getLength();
        int minutes = time / 60, seconds = time % 60;

        holder.tvName.setText(song.getName());
        holder.tvArtist.setText(song.getArtist());
        holder.tvTime.setText(String.format(Locale.getDefault(),
                "%d:%s", minutes, (seconds > 9) ? "" + seconds : "0" + seconds));

        switch (mFragmentId) {
            case R.layout.fragment_my_playlist:
                holder.btnDelete.setVisibility(View.VISIBLE);
                break;
            case R.layout.fragment_search:
                holder.btnAdd.setVisibility(View.VISIBLE);
                break;
        }
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnClickListener != null){
                    mOnClickListener.onViewClick(pos);
                }
            }
        });
        holder.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnClickListener != null){
                    mOnClickListener.onButtonAddClick(pos);
                }
            }
        });
        holder.btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnClickListener != null){
                    mOnClickListener.onButtonDownloadClick(pos);
                }
            }
        });
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnClickListener != null){
                    mOnClickListener.onButtonDeleteClick(pos);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mSongs.size();
    }

    void setOnClickListener(OnClickListener listener) {
        mOnClickListener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        View view;

        TextView
                tvName,
                tvArtist,
                tvTime;
        ImageButton
                btnAdd,
                btnDownload,
                btnDelete;

        ViewHolder(View itemView) {
            super(itemView);

            view = itemView;
            tvName = (TextView) itemView.findViewById(R.id.textView_itemSong_name);
            tvArtist = (TextView) itemView.findViewById(R.id.textView_itemSong_artist);
            tvTime = (TextView) itemView.findViewById(R.id.textView_itemSong_time);
            btnAdd = (ImageButton) itemView.findViewById(R.id.imageButton_itemSong_add);
            btnDelete = (ImageButton) itemView.findViewById(R.id.imageButton_itemSong_delete);
            btnDownload = (ImageButton) itemView.findViewById(R.id.imageButton_itemSong_download);
        }
    }
}

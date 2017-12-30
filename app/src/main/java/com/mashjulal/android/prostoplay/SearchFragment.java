package com.mashjulal.android.prostoplay;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mashjulal.android.prostoplay.prosto_pleer.DownloadLinkModel;
import com.mashjulal.android.prostoplay.prosto_pleer.ProstoPleerClient;
import com.mashjulal.android.prostoplay.prosto_pleer.Track;
import com.mashjulal.android.prostoplay.prosto_pleer.TrackListModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SearchFragment extends Fragment {

    private static final String TAG = SearchFragment.class.getSimpleName();

    private static final int RESULT_ON_PAGE = 15;

    RecyclerView rvFoundTracks;
    TextView tvEmpty;
    private List<Song> mSongs = new ArrayList<>();
    private SongItemRecyclerViewAdapter mSongAdapter;
    LinearLayoutManager mLayoutManager;
    private ProstoPleerClient mClient;
    private int page;
    private int tracksCount;
    private String mCurrentQuery;
    private MusicPlayerService mPlayerService;
    private FloatingActionButton fabUp;
    private RecyclerView.SmoothScroller smoothScroller;
    private Context mContext;

    private EndlessRecyclerOnScrollListener endlessRecyclerOnScrollListener;


    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        mContext = container.getContext();
        mPlayerService = ((MainActivity) getActivity()).getMusicPlayerService();

        rvFoundTracks = (RecyclerView) view.findViewById(R.id.fragment_search_rv_found_tracks);
        tvEmpty = (TextView) view.findViewById(R.id.fragment_search_tv_empty);
        fabUp = (FloatingActionButton) view.findViewById(R.id.floatingActionButton_search_up);
        setHasOptionsMenu(true);

        tvEmpty.setVisibility(View.GONE);
        rvFoundTracks.setVisibility(View.GONE);
        mClient = ProstoPleerClient.getInstance(mContext);

        mSongAdapter = new SongItemRecyclerViewAdapter(mSongs, R.layout.fragment_search);

        mLayoutManager = new LinearLayoutManager(getActivity());
        rvFoundTracks.setLayoutManager(mLayoutManager);
        rvFoundTracks.addItemDecoration(
                new DividerItemDecoration(mContext, mLayoutManager.getOrientation()));
        rvFoundTracks.setAdapter(mSongAdapter);

        smoothScroller = new LinearSmoothScroller(mContext) {
            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };
        rvFoundTracks.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (mLayoutManager.findFirstVisibleItemPosition() > 10)
                    fabUp.setVisibility(View.VISIBLE);
                else
                    fabUp.setVisibility(View.GONE);
            }
        });
        endlessRecyclerOnScrollListener = new EndlessRecyclerOnScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                searchTracks();
            }
        };
        rvFoundTracks.addOnScrollListener(endlessRecyclerOnScrollListener);

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

        if (!mClient.hasAccessToken()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                    .setTitle(getResources().getString(R.string.app_name))
                    .setMessage("Cannot connect to pleer.com. Please try again later.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            builder.create().show();
        }

        fabUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                smoothScroller.setTargetPosition(0);
                mLayoutManager.startSmoothScroll(smoothScroller);
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        if (!mClient.hasAccessToken())
            return;
        inflater.inflate(R.menu.menu_fragment_search, menu);

        MenuItem myActionMenuItem = menu.findItem(R.id.item_menuFragmentSearch_search);
        SearchView svTracks = (SearchView) myActionMenuItem.getActionView();
        svTracks.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchTracks(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    void searchTracks(String query) {
        if (mCurrentQuery != null && mCurrentQuery.equals(query))
            return;
        page = 1;
        endlessRecyclerOnScrollListener.reset();
        mCurrentQuery = query;
        mClient.searchTracks(mCurrentQuery, page, RESULT_ON_PAGE).enqueue(new Callback<TrackListModel>() {
            @Override
            public void onResponse(Call<TrackListModel> call, Response<TrackListModel> response) {
                TrackListModel model = response.body();
                if (model != null) {
                    tracksCount = model.getCount();
                    populateList(model.getTrackMap().values());
                }
            }

            @Override
            public void onFailure(Call<TrackListModel> call, Throwable t) {

            }
        });
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
                    TrackLab.getInstance(mContext).addSong(song);
                    Toast.makeText(
                            mContext,
                            String.format("Song \"%s\" was added to your playlist", song.getName()),
                            Toast.LENGTH_SHORT)
                            .show();
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
        TrackLab.getInstance(mContext).removeSong(song.getId());
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

    private void populateList(Collection<Track> tracks) {
        if (page == 1) {
            mSongs.clear();
        }
        if (tracks != null) {
            List<Song> songs = toSongList(tracks);
            mSongs.addAll(songs);
        }
        if (mSongs.size() == 0) {
            rvFoundTracks.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            rvFoundTracks.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }

        if (page == 1) {
            mSongAdapter.notifyDataSetChanged();
            mLayoutManager.scrollToPosition(0);
        } else {
            mSongAdapter.notifyItemInserted(mSongs.size() - 1);
        }
    }

    void searchTracks() {
        if (mSongs.size() >= tracksCount) {
            return;
        }
        page++;
        mClient.searchTracks(mCurrentQuery, page, RESULT_ON_PAGE).enqueue(new Callback<TrackListModel>() {
            @Override
            public void onResponse(Call<TrackListModel> call, Response<TrackListModel> response) {
                TrackListModel model = response.body();
                if (model != null)
                    populateList(model.getTrackMap().values());
            }

            @Override
            public void onFailure(Call<TrackListModel> call, Throwable t) {
            }
        });
    }

    private List<Song> toSongList(Collection<Track> tracks) {
        List<Song> songs = new ArrayList<>();
        for (Track track : tracks) {
            songs.add(new Song(track));
        }
        return songs;
    }
}

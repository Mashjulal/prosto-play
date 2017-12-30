package com.mashjulal.android.prostoplay;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String CURRENT_FRAGMENT = "current_fragment";


    private FragmentManager fm;
    private MusicPlayerFragment musicPlayer;
    private NavigationView navigationView;
    private int menuItemID = R.id.activity_menu_drawer_my_playlist;
    private Bundle sisb;

    private MusicPlayerService mPlayerService;

    private ServiceConnection mPlayerConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPlayerService = ((MusicPlayerService.MusicPlayerBinder) service).getService();

            musicPlayer = MusicPlayerFragment.newInstance();
            fm.beginTransaction().add(R.id.activity_main_fr_music_player, musicPlayer).commit();

            setFragmentAtId((sisb != null) ?
                    sisb.getInt(CURRENT_FRAGMENT) :
                    menuItemID);
            sisb = null;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mPlayerService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindPlayerService();
        updateToken();

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);

        configureDrawerAndToolbar();

        fm = getFragmentManager();
        sisb = savedInstanceState;
    }

    private void updateToken() {
        Intent i = new Intent(this, UpdateAccessTokenReceiver.class);
        sendBroadcast(i);
    }

    private void configureDrawerAndToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void bindPlayerService() {
        Intent intent = new Intent(this, MusicPlayerService.class);
        startService(intent);
        bindService(intent, mPlayerConn, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        menuItemID = id;

        fm.beginTransaction()
                .replace(R.id.activity_main_fragment_container, getFragmentById(id))
                .commit();
        setFragmentTitle(id);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        ProstoPlayNotification.cancel(getApplicationContext());
        if (mPlayerService != null) {
            unbindService(mPlayerConn);
            mPlayerService = null;
        }

        Intent i = new Intent(this, UpdateAccessTokenReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.cancel(pi);

        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(CURRENT_FRAGMENT, menuItemID);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private Fragment getFragmentById(int id){
        Fragment fragment = null;
        switch (id){
            case R.id.activity_menu_drawer_my_playlist:
                fragment = MyPlaylistFragment.newInstance();
                break;
            case R.id.activity_menu_drawer_search:
                fragment = SearchFragment.newInstance();
                break;
        }
        return fragment;
    }

    private void setFragmentAtId(int id){
        fm.beginTransaction()
                .add(R.id.activity_main_fragment_container, getFragmentById(id))
                .commit();
        int menu_id = 0;
        switch (id){
            case R.id.activity_menu_drawer_my_playlist:
                menu_id = 0;
                getSupportActionBar().setTitle("My Playlist");
                break;
            case R.id.activity_menu_drawer_search:
                menu_id = 1;
                getSupportActionBar().setTitle("Search Tracks");
                break;
        }
        navigationView.getMenu().getItem(menu_id).setChecked(true);
    }

    private void setFragmentTitle(int id) {
        int titleId = 0;
        switch (id){
            case R.id.activity_menu_drawer_my_playlist:
                titleId = R.string.title_my_playlist_fragment;
                break;
            case R.id.activity_menu_drawer_search:
                titleId = R.string.title_search_fragment;
                break;
        }
        getSupportActionBar().setTitle(titleId);
    }

    MusicPlayerService getMusicPlayerService(){
        return mPlayerService;
    }
}

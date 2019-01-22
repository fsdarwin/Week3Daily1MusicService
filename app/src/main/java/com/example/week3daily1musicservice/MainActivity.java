package com.example.week3daily1musicservice;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.MediaController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity implements MediaController.MediaPlayerControl {

    private MusicService musicService;
    private Intent playIntent;
    private boolean musicBound = false;
    private ArrayList<Song> songList;
    private ListView songView;
    private MusicController musicController;
    private boolean paused = false;
    private boolean playbackPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        songView = findViewById(R.id.song_list);
        songList = new ArrayList<>();
        getSongList();
        Collections.sort(songList, new Comparator<Song>() {
            @Override
            public int compare(Song o1, Song o2) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });
        SongAdapter songAdapter = new SongAdapter(this, songList);
        songView.setAdapter(songAdapter);
        setController();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            musicService.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    public void getSongList() {
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if ((musicCursor != null) && musicCursor.moveToFirst()) {
            int titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist));
            } while(musicCursor.moveToNext());
        }
    }

    public void songPicked(View view) {
        musicService.setSong(Integer.parseInt(view.getTag().toString()));
        musicService.playSong();
        if(playbackPaused){
            setController();
            playbackPaused = false;
        }
        musicController.show(0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_end:
                stopService(playIntent);
                musicService = null;
                System.exit(0);
                break;
            case R.id.action_shuffle:
                musicService.setShuffle();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicService = null;
        super.onDestroy();
    }

    private void setController() {
        musicController = new MusicController(this);
        musicController.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
        musicController.setMediaPlayer(this);
        musicController.setAnchorView(findViewById(R.id.song_list));
        musicController.setEnabled(true);
    }

    private void playNext() {
        musicService.playNext();
        if(playbackPaused){
            setController();
            playbackPaused = false;
        }
        musicController.show(0);
    }

    private void playPrev() {
        musicService.playPrev();
        if(playbackPaused){
            setController();
            playbackPaused = false;
        }
        musicController.show(0);
    }

    @Override
    public void start() {
        musicService.go();
    }

    @Override
    public void pause() {
        playbackPaused = true;
        musicService.pausePlayer();
    }

    @Override
    public int getDuration() {
        if (musicService != null && musicBound && musicService.isPlaying()) {
            return musicService.getDuration();
        } else {
            return 0;
        }
    }

    @Override
    public int getCurrentPosition() {
        if (musicService != null && musicBound && musicService.isPlaying()) {
            return musicService.getPosition();
        } else {
            return 0;
        }
    }

    @Override
    public void seekTo(int pos) {
        musicService.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if (musicService != null && musicBound) {
            return musicService.isPlaying();
        } else {
            return false;
        }
    }
    protected void onPause(){
        super.onPause();
        paused = true;
    }
    protected void onResume(){
        super.onResume();
        if(paused){
            setController();
            paused = false;
        }
    }
    protected void onStop(){
        musicController.hide();
        super.onStop();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}

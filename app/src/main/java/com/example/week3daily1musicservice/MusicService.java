package com.example.week3daily1musicservice;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    public static final String TAG = "FRANK: ";

    private MediaPlayer mediaPlayer;
    private ArrayList<Song> songs;
    private int songPosition;
    private final IBinder musicBind = new MusicBinder();
    private String songTitle = "";
    private static final int NOTIFY_ID = 1;
    private boolean shuffle = false;
    private Random random;

    @Override
    public void onCreate() {
        super.onCreate();
        songPosition = 0;
        mediaPlayer = new MediaPlayer();
        initMusicPlayer();
        random = new Random();
    }

    public void onDestroy() {
        stopForeground(true);
    }

    public void initMusicPlayer() {
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mediaPlayer.stop();
        mediaPlayer.release();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendIntent = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendIntent)
                .setSmallIcon(R.drawable.play)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle);
        Notification notification = builder.build();

        startForeground(NOTIFY_ID, notification);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(mediaPlayer.getCurrentPosition() > 0){
            mp.reset();
            playNext();
        }
    }

    public void setList(ArrayList<Song> songs) {
        this.songs = songs;
    }

    public void setSong(int songIndex) {
        songPosition = songIndex;
    }

    public void playSong() {
        mediaPlayer.reset();
        Song playing = songs.get(songPosition);
        songTitle = playing.getTitle();
        long current = playing.getId();
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                current);
        try {
            mediaPlayer.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e(TAG, "playSong: Error with data service", e);
        }
        mediaPlayer.prepareAsync();
    }

    public void setShuffle() {
        if (shuffle) {
            shuffle = false;
        } else {
            shuffle = true;
        }
    }

    public int getPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void pausePlayer() {
        mediaPlayer.pause();
    }

    public void seek(int nextPosition) {
        mediaPlayer.seekTo(nextPosition);
    }

    public void go() {
        mediaPlayer.start();
    }

    public void playPrev() {
        songPosition--;
        if (songPosition < 0) {
            songPosition = songs.size() - 1;
            playSong();
        }
    }

    public void playNext() {
        if (shuffle) {
            int newSong = songPosition;
            while (newSong == songPosition) {
                newSong = random.nextInt(songs.size());
            }
            songPosition = newSong;
        } else {
            songPosition++;
            if (songPosition >= songs.size()) {
                songPosition = 0;
            }
            playSong();
        }
    }


    ////----SubClass------------------------------------------------------------------------------------
    public class MusicBinder extends Binder {

        MusicService getService() {
            return MusicService.this;
        }
    }
}

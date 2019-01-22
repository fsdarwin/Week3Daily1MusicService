package com.example.week3daily1musicservice;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class SongAdapter extends BaseAdapter {

    private ArrayList<Song> songs;
    private LayoutInflater songInflater;

    public SongAdapter(Context context, ArrayList<Song> songList) {
        songs = songList;
        songInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout songLayout = (LinearLayout)songInflater.inflate(R.layout.song, parent, false);
        TextView songView = (TextView)songLayout.findViewById(R.id.song_title);
        TextView artistView = (TextView)songLayout.findViewById(R.id.song_artist);
        Song current = songs.get(position);
        songView.setText(current.getTitle());
        artistView.setText(current.getArtist());
        songLayout.setTag(position);
        return songLayout;
    }
}

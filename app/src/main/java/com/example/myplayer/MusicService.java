package com.example.myplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.LongDef;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MusicService extends Service {
    private MediaPlayer mediaPlayer;
    private List<Map<String, String>> songList;
    private int currentSongIndex;
    private IBinder binder = new MusicBinder();

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return this.binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("TAG", "MusicService创建");

        mediaPlayer = new MediaPlayer();
        songList = new ArrayList<>();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                nextSong();
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("TAG", "MusicService启动");
        if (intent != null && intent.hasExtra("songPath")) {
            String songName = intent.getStringExtra("songName");
            String songImage_circle = intent.getStringExtra("songImage_circle");
            String songArtist = intent.getStringExtra("songArtist");
            String songPath = intent.getStringExtra("songPath");
            Map<String, String> map = new HashMap<>();
            map.put("songName", songName);
            map.put("songImage_circle", songImage_circle);
            map.put("songArtist", songArtist);
            map.put("songPath", songPath);
            if (!songList.contains(map)) {
                songList.add(map);
                currentSongIndex = songList.size() - 1;
            } else {
                currentSongIndex = songList.indexOf(map);
            }
            play();
            Log.d("TAG", "播放列表: " + songList);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("TAG", "MusicService销毁");
    }

    public void play() {
        try {
            String songPath = songList.get(currentSongIndex).get("songPath");
            mediaPlayer.reset();
            mediaPlayer.setDataSource(songPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void seekTo(int progress) {
        mediaPlayer.seekTo(progress);
    }

    public void pause() {
        if (mediaPlayer.isPlaying()) mediaPlayer.pause();
    }

    public void noPause() {
        if (!mediaPlayer.isPlaying()) mediaPlayer.start();
    }

    public void lastSong() {
        if (currentSongIndex > 0) currentSongIndex--;
        else currentSongIndex = songList.size() - 1;
        String songName = getCurrentSongName();
        String songImageCircle = getCurrentSongImageCircle();
        sendSongImageCircleBroadcast(songName, songImageCircle);
        play();
    }

    public void nextSong() {
        if (currentSongIndex < songList.size() - 1) currentSongIndex++;
        else currentSongIndex = 0;
        String songName = getCurrentSongName();
        String songImageCircle = getCurrentSongImageCircle();
        sendSongImageCircleBroadcast(songName, songImageCircle);
        play();
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public String getCurrentSongName() {
        if (songList.size() > 0 && currentSongIndex >= 0 && currentSongIndex < songList.size()) {
            return songList.get(currentSongIndex).get("songName");
        }
        return null;
    }

    public String getCurrentSongImageCircle() {
        if (songList.size() > 0 && currentSongIndex >= 0 && currentSongIndex < songList.size()) {
            return songList.get(currentSongIndex).get("songImage_circle");
        }
        return null;
    }

    private void sendSongImageCircleBroadcast(String songName, String songImageCircle) {
        Intent intent = new Intent("com.example.myplayer.SONG_IMAGE_CIRCLE");
        intent.putExtra("songName", songName);
        intent.putExtra("songImageCircle", songImageCircle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}

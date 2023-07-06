package com.example.myplayer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class PlayActivity extends AppCompatActivity {
    private boolean isBound = false;
    private boolean isPlaying = true;
    private ServiceConnection serviceConnection;
    private BroadcastReceiver songImageCircleReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String songName = intent.getStringExtra("songName");
            String songImageCircle = intent.getStringExtra("songImageCircle");
            updateSongInfo(songName, songImageCircle);
        }
    };
    private MusicService.MusicBinder binder;
    private MusicService musicService;
    private String songName;
    private String songImage_circle;
    private String songArtist;
    private String songPath;
    private Animation animation;
    private SeekBar seekBar;
    private TextView nameTextView;
    private ImageView imageView;
    private TextView progressTextView;
    private TextView totalTextView;
    private ImageButton pauseButton;
    private ImageButton lastSongButton;
    private ImageButton nextSongButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));

        songName = getIntent().getStringExtra("songName");
        songImage_circle = getIntent().getStringExtra("songImage_circle");
        songArtist = getIntent().getStringExtra("songArtist");
        songPath = getIntent().getStringExtra("songPath");
        nameTextView = findViewById(R.id.NameTextView);
        imageView = findViewById(R.id.imageView);
        seekBar = findViewById(R.id.seekBar);
        progressTextView = findViewById(R.id.progressTextView);
        totalTextView = findViewById(R.id.totalTextView);
        pauseButton = findViewById(R.id.pauseButton);
        lastSongButton = findViewById(R.id.lastSongButton);
        nextSongButton = findViewById(R.id.nextSongButton);
        IntentFilter filter = new IntentFilter("com.example.myplayer.SONG_IMAGE_CIRCLE");
        LocalBroadcastManager.getInstance(this).registerReceiver(songImageCircleReceiver, filter);

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binder = (MusicService.MusicBinder) service;
                musicService = binder.getService();
                isBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                isBound = false;
            }
        };

        animation = AnimationUtils.loadAnimation(this, R.anim.rotate_clockwise);
        int resId = getResources().getIdentifier(songImage_circle, "drawable", getPackageName());
        nameTextView.setText(songName);
        imageView.setImageResource(resId);
        imageView.startAnimation(animation);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    musicService.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound) {
                    if (isPlaying) {
                        musicService.pause();
                        isPlaying = false;
                        pauseButton.setImageResource(R.drawable.round_play_arrow_24);
                        imageView.clearAnimation();
                    } else {
                        musicService.noPause();
                        isPlaying = true;
                        pauseButton.setImageResource(R.drawable.round_pause_24);
                        imageView.startAnimation(animation);
                    }
                }
            }
        });

        lastSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound)
                    musicService.lastSong();
            }
        });

        nextSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound)
                    musicService.nextSong();
            }
        });

        updateSeekBar();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 绑定 MusicService
        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra("songName", songName);
        intent.putExtra("songImage_circle", songImage_circle);
        intent.putExtra("songArtist", songArtist);
        intent.putExtra("songPath", songPath);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        startService(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 解绑 MusicService
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销广播接收器
        LocalBroadcastManager.getInstance(this).unregisterReceiver(songImageCircleReceiver);
    }

    private void updateSeekBar() {
        new Thread(() -> {
            while (isPlaying) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(() -> {
                    int currentPosition = musicService.getCurrentPosition();
                    int totalDuration = musicService.getDuration();
                    int durationMinutes = (totalDuration / 1000) / 60;
                    int durationSeconds = (totalDuration / 1000) % 60;
                    int currentPositionMinutes = (currentPosition / 1000) / 60;
                    int currentPositionSeconds = (currentPosition / 1000) % 60;
                    String durationString = String.format("%02d:%02d", durationMinutes, durationSeconds);
                    String currentPositionString = String.format("%02d:%02d", currentPositionMinutes, currentPositionSeconds);
                    progressTextView.setText("" + currentPositionString);
                    totalTextView.setText("" + durationString);
                    seekBar.setMax(totalDuration);
                    seekBar.setProgress(currentPosition);
                });
            }
        }).start();
    }

    private void updateSongInfo(String songName, String songImageCircle) {
        nameTextView.setText(songName);
        int resId = getResources().getIdentifier(songImageCircle, "drawable", getPackageName());
        imageView.setImageResource(resId);
    }
}

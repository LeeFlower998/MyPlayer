package com.example.myplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ListView songListView;
    private SimpleAdapter songListAdapter;
    private List<Map<String, String>> songList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));

        songListView = findViewById(R.id.songListView);
        songList = initData();

        songListAdapter = new SimpleAdapter(
                this,
                songList,
                R.layout.activity_songlist,
                new String[]{"songImage", "songName", "songArtist", "songURL"},
                new int[]{R.id.songImageView, R.id.songNameTextView, R.id.songArtistTextView, R.id.songURLTextView});
        songListAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (view instanceof ImageView && data instanceof Integer) {
                    ImageView imageView = (ImageView) view;
                    int resourceId = (int) data;
                    imageView.setImageResource(resourceId);
                    return true;
                }
                return false;
            }
        });
        songListView.setAdapter(songListAdapter);

        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                checkAndPlaySong(position);
            }
        });
    }

    private void checkAndPlaySong(int position) {
        // 检查歌曲文件是否存在，如果存在直接进入播放页面，如果不存在则下载后再进入播放页面
        String songName = songList.get(position).get("songName");
        String songImage_circle = songList.get(position).get("songImage_circle");
        String songArtist = songList.get(position).get("songArtist");
        String songURL = songList.get(position).get("songURL");
        String songPath = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), songName + ".mp3").getPath();
        boolean songExists = checkSongExists(songName);
        if (songExists) {
            // 进入播放页面
            Intent intent = new Intent(MainActivity.this, PlayActivity.class);
            intent.putExtra("songName", songName);
            intent.putExtra("songImage_circle", songImage_circle);
            intent.putExtra("songArtist", songArtist);
            intent.putExtra("songPath", songPath);
            startActivity(intent);
            Toast.makeText(this, "歌曲正在播放", Toast.LENGTH_SHORT).show();
        } else {
            // 启动下载服务下载歌曲
            Intent intent = new Intent(MainActivity.this, DownloadService.class);
            intent.putExtra("songName", songName);
            intent.putExtra("songURL", songURL);
            startService(intent);
            Toast.makeText(this, "歌曲正在下载，请稍候...", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkSongExists(String songName) {
        File musicSaveDirectory = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), songName + ".mp3");
        boolean isDownloaded = musicSaveDirectory.exists();
        if (isDownloaded) {
            // 歌曲已经下载
            Log.d("TAG", "已下载: " + songName);
            Log.d("TAG", "保存路径: " + musicSaveDirectory);
            return true;
        } else {
            // 歌曲未下载
            Log.d("TAG", songName + " 未下载");
            return false;
        }
    }

    private List<Map<String, String>> initData() {
        List<Map<String, String>> list = new ArrayList<>();
        Map<String, String> map;

        map = new HashMap<>();
        map.put("songImage", "" + R.drawable.picture1);
        map.put("songImage_circle", "" + R.drawable.picture1_circle);
        map.put("songName", "可爱女人");
        map.put("songArtist", "周杰伦");
        map.put("songURL", "https://s11.lanzoug.com/07042300111341261bb/2023/04/17/ab1e4671e677c8a7ab02221e29407083.mp3?st=CeU1_xNp5zYhMpuXq_z_Sw&e=1688484879&b=BLNc01D_aB_bVSj1ewALQHuVG_bC3pXeAUtA34LWVc8UTJVaVxyUj8Cdwdh&fi=111341261&pid=123-124-246-127&up=2&mp=0&co=1");
        list.add(map);

        map = new HashMap<>();
        map.put("songImage", "" + R.drawable.picture1);
        map.put("songImage_circle", "" + R.drawable.picture1_circle);
        map.put("songName", "反方向的钟");
        map.put("songArtist", "周杰伦");
        map.put("songURL", "https://develope8.lanzoug.com/0704220089085378bb/2022/11/16/0496bf8b971261f8ba5aada3afea0948.mp3?st=r8QY4j6T0xc0wNZrT7CI1Q&e=1688483722&b=U_bQIhwKPVrVSkQfoVuNQxAnNDrUEnwKAALoOkVzEAyxT5AmYVf0H5FaeAOZW4gO7APMKeAU3UGRSMQopVz0DcVMy&fi=89085378&pid=123-124-246-127&up=2&mp=0&co=1");
        list.add(map);

        map = new HashMap<>();
        map.put("songImage", "" + R.drawable.picture2);
        map.put("songImage_circle", "" + R.drawable.picture2_circle);
        map.put("songName", "简单爱");
        map.put("songArtist", "周杰伦");
        map.put("songURL", "https://develope8.lanzoug.com/0704220088960578bb/2022/11/14/e9efdc9ca636ab56e5fa16b2f3fbd39f.mp3?st=8kmKCGEIZAulm5pgi43uxw&e=1688484591&b=VuNc8gCAULBTi1OQBrEFiQPnD35RtQKVUaoJ4lbMVuQHsQ6yUPZSeVg_bVzMGZlN5UmoJJQY0&fi=88960578&pid=123-124-246-127&up=2&mp=0&co=1");
        list.add(map);

        map = new HashMap<>();
        map.put("songImage", "" + R.drawable.picture2);
        map.put("songImage_circle", "" + R.drawable.picture2_circle);
        map.put("songName", "开不了口");
        map.put("songArtist", "周杰伦");
        map.put("songURL", "https://develope3.lanzoug.com/0704220088985473bb/2022/11/15/c573c953d05be8475cca8dc81924c10b.mp3?st=oifB_Uy4ile0Tu5_AnWCRA&e=1688484512&b=BLNe4gSEU7IC71eMBbFV6wnaC7JQ3gShAX9asl3LA6kBtVvGA7NUtQfuU6MBfw84CW4LZwcoUDsAIwk3&fi=88985473&pid=123-124-246-127&up=2&mp=0&co=1");
        list.add(map);

        map = new HashMap<>();
        map.put("songImage", "" + R.drawable.picture3);
        map.put("songImage_circle", "" + R.drawable.picture3_circle);
        map.put("songName", "我落泪情绪零碎");
        map.put("songArtist", "周杰伦");
        map.put("songURL", "https://s31.lanzoug.com:446/07051500117237843bb/2023/05/21/b5d68f76ce7c3bafa2795935d3a1783b.mp3?st=_hMVDTJK0QpMXpM7kobz2w&e=1688544982&b=B7MLg1DBU75ZnATvBLIFsgX6DLYGhFHSBLBf6QGsUbpTmgq8A_bQEowfcVC8HsgOWA_a4KsAaaA7UFsgm4VfRTfwdkCzlQaFN4WWEEIgRn&fi=117237843&pid=123-124-246-127&up=2&mp=0&co=1");
        list.add(map);

        map = new HashMap<>();
        map.put("songImage", "" + R.drawable.picture3);
        map.put("songImage_circle", "" + R.drawable.picture3_circle);
        map.put("songName", "好久不见");
        map.put("songArtist", "周杰伦");
        map.put("songURL", "https://develope4.lanzoug.com/0704220090994084bb/2022/11/28/239c19480fef52d7812aaf2b1e82dac5.mp3?st=lLHwsZgYrlAGZqR_2h_Jng&e=1688484658&b=BrEKr1PuV7ZWuleEUuYPswffW_b8Bp1fQBHpaslLEVP4AtAGcBbVTslK7AvJRL1BnA2RaNlN8UzgEJwA_b&fi=90994084&pid=123-124-246-127&up=2&mp=0&co=1");
        list.add(map);

        return list;
    }
}

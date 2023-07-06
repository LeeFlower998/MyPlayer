package com.example.myplayer;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadService extends IntentService {
    private static final String TAG = "DownloadService";

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String songName = intent.getStringExtra("songName");
        Log.d("TAG", "songName: " + songName);
        String songURL = intent.getStringExtra("songURL");
        Log.d("TAG", "songURL: " + songURL);
        downloadFile(songName, songURL);
    }

    private void downloadFile(String songName, String songURL) {
        try {
            URL downloadUrl = new URL(songURL);
            HttpURLConnection connection = (HttpURLConnection) downloadUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.connect();

            int fileLength = connection.getContentLength();

            // 获取应用私有目录
            File musicDirectory = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), songName + ".mp3");
            FileOutputStream outputStream = new FileOutputStream(musicDirectory);

            InputStream inputStream = connection.getInputStream();
            byte[] buffer = new byte[40960];
            int bytesRead;
            int totalBytesRead = 0;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;

                // 计算下载进度
                int progress = (int) ((totalBytesRead / (float) fileLength) * 100);
                Log.d("TAG", "Download progress: " + progress + "%");
            }
            outputStream.close();
            inputStream.close();
            Log.d("TAG", "File downloaded successfully");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

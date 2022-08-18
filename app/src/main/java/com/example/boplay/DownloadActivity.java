package com.example.boplay;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.SparseArray;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

public class DownloadActivity extends AppCompatActivity {

    private static final int ITAG_FOR_AUDIO = 140;

    String link = "";

    YouTubePlayerView youTubePlayerView;
    Button btn_download, btn_play;
    EditText link_et;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        btn_download = findViewById(R.id.download_button);
        btn_play = findViewById(R.id.play_button);
        link_et = findViewById(R.id.youtube_link_et);

        youTubePlayerView = findViewById(R.id.youtube_play);
        getLifecycle().addObserver(youTubePlayerView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        youTubePlayerView.release();
    }


    @Override
    protected void onPause() {
        super.onPause();
        youTubePlayerView.release();
    }

    public void playButton(View v) {
        link = link_et.getText().toString();
        if (link != null
                && (link.contains("://youtu.be/")
                || link.contains("youtube.com/watch?v="))) {
            youTubePlayerView.setVisibility(View.VISIBLE);
            String videoId = getVideoID(link);
            youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                @Override
                public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                    super.onReady(youTubePlayer);
                    youTubePlayer.cueVideo(videoId, 0);
                }
            });
        } else {
            Toast.makeText(this, R.string.error_no_yt_link, Toast.LENGTH_SHORT).show();
        }
    }
    public String getVideoID(String id){
        int startIndex=id.indexOf("v=")+2;
        int stopIndex=startIndex+11;
        String sub=id.substring(startIndex,stopIndex);
        return sub;
    }

    public void downloadButton(View v) {
        link = link_et.getText().toString();
        if (link != null
                && (link.contains("://youtu.be/")
                || link.contains("youtube.com/watch?v="))) {
            download();
        } else {
            Toast.makeText(this, R.string.error_no_yt_link, Toast.LENGTH_SHORT).show();
        }
    }

    public void download() {
        link = link_et.getText().toString();
        new YouTubeExtractor(this) {
            @Override
            public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
                if (ytFiles != null) {
                    String downloadUrl = ytFiles.get(ITAG_FOR_AUDIO).getUrl();
                    DownloadManager downloadmanager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
                    request.setTitle("Youtube MP");
                    request.setDescription("Downloading");
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setVisibleInDownloadsUi(false);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS
                            , getVideoID(link)+".mp3");
                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE
                            | DownloadManager.Request.NETWORK_WIFI);
                    downloadmanager.enqueue(request);
                }
            }
        }.extract(link);
    }
}


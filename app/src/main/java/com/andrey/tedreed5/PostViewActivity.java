package com.andrey.tedreed5;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.MediaController;
import android.widget.VideoView;

/**
 * Created by andrey on 24.04.15.
 */
public class PostViewActivity extends Activity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //gets the array list and bundles it
        this.setContentView(R.layout.postview);
        Bundle bundle = this.getIntent().getExtras();


        String postContent = bundle.getString("content");
        String postContentVideo = bundle.getString("video");

        Log.d("PVA", "postContentVideo: " + postContentVideo);

        VideoView vidView = (VideoView)findViewById(R.id.myVideo);
        String vidAddress = postContentVideo;
        Uri vidUri = Uri.parse(vidAddress);
        vidView.setVideoURI(vidUri);

        MediaController vidControl = new MediaController(this);
        vidControl.setAnchorView(vidView);
        vidView.setMediaController(vidControl);

        vidView.start();
    }


    @Override
    protected void onDestroy() {

        super.onDestroy();
    }
}

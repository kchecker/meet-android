package com.example.jitsiandroid;

import android.content.Intent;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.jitsi.meet.sdk.JitsiMeetView;
import org.jitsi.meet.sdk.JitsiMeetViewListener;


import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private JitsiMeetView view;

    private FrameLayout jitsi_layout;

    private   BallBounces ball;

    @Override
    public void onBackPressed() {
        if (!JitsiMeetView.onBackPressed()) {
            // Invoke the default handler if it wasn't handled by React.
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        view = new JitsiMeetView(this);
        view.setWelcomePageEnabled(true);//set jitsi-meet

        Bundle config = new Bundle();
        config.putBoolean("startWithAudioMuted", false);
        config.putBoolean("startWithVideoMuted", false);
        Bundle urlObject = new Bundle();
        urlObject.putBundle("config", config);
        //urlObject.putString("url", "https://meet.meetrix.xyz/12345");
        urlObject.putString("url", "https://meet.jit.si/1234");
        view.loadURLObject(urlObject);

       setContentView(R.layout.activity_main);
        jitsi_layout=(FrameLayout) this.findViewById(R.id.jitsi_content);
        FrameLayout.LayoutParams lparams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lparams);

//        jitsi_layout.removeViewAt(0);
        this.jitsi_layout.addView(view);
        //this.jitsi_layout.addView(text);

        ball = new BallBounces(this);
//        ball.setBackgroundColor(Color.TRANSPARENT);
        jitsi_layout.addView(ball);

        //run togetherjs in a seperate thread--------------------------------------------------
        Thread t = new Thread(new Runnable() {
            public void run()
            {
                WebSocketEcho webSocketEcho = new WebSocketEcho();
                try {
                    webSocketEcho.run();
                    //register ball bounce observer
                    webSocketEcho.registerObserver(ball);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }});
        t.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        view.dispose();
        view = null;

        JitsiMeetView.onHostDestroy(this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        JitsiMeetView.onNewIntent(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();

        JitsiMeetView.onHostPause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        JitsiMeetView.onHostResume(this);
    }
}

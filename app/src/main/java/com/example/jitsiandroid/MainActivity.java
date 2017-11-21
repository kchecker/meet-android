package com.example.jitsiandroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetView;
import org.jitsi.meet.sdk.JitsiMeetViewListener;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

//speechRecognize
import android.os.Bundle;
//import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends JitsiMeetActivity implements SpeechRecognizerManager.OnResultListener{
    private final String TAG = getClass().getSimpleName();
    private ImageView lightBulbImageView;
    private SpeechRecognizerManager mSpeechRecognizerManager;
    @Override
    protected JitsiMeetView initializeView() {
        JitsiMeetView view = super.initializeView();

        // XXX In order to increase (1) awareness of API breakages and (2) API
        // coverage, utilize JitsiMeetViewListener in the Debug configuration of
        // the app.
        if (BuildConfig.DEBUG && view != null) {
            view.setListener(new JitsiMeetViewListener() {
                private void on(String name, Map<String, Object> data) {
                    // Log with the tag "ReactNative" in order to have the log
                    // visible in react-native log-android as well.
                    Log.d(
                            "ReactNative",
                            JitsiMeetViewListener.class.getSimpleName() + " "
                                    + name + " "
                                    + data);
                }

                @Override
                public void onConferenceFailed(Map<String, Object> data) {
                    on("CONFERENCE_FAILED", data);
                }

                @Override
                public void onConferenceJoined(Map<String, Object> data) {
                    on("CONFERENCE_JOINED", data);
                }

                @Override
                public void onConferenceLeft(Map<String, Object> data) {
                    on("CONFERENCE_LEFT", data);
                }

                @Override
                public void onConferenceWillJoin(Map<String, Object> data) {
                    on("CONFERENCE_WILL_JOIN", data);
                }

                @Override
                public void onConferenceWillLeave(Map<String, Object> data) {
                    on("CONFERENCE_WILL_LEAVE", data);
                }

                @Override
                public void onLoadConfigError(Map<String, Object> data) {
                    on("LOAD_CONFIG_ERROR", data);
                }
            });
        }

        Bundle config = new Bundle();
        config.putBoolean("startWithAudioMuted", false);
        config.putBoolean("startWithVideoMuted", false);
        Bundle urlObject = new Bundle();
        urlObject.putBundle("config", config);
        urlObject.putString("url", "https://meet.jit.si/1234");
        view.loadURLObject(urlObject);

        return view;
    }


    @Override
    public void onBackPressed() {
        if (!JitsiMeetView.onBackPressed()) {
            // Invoke the default handler if it wasn't handled by React.
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setWelcomePageEnabled(true);
        super.onCreate(savedInstanceState);

        //speech recognize
        setContentView(R.layout.activity_main);

        lightBulbImageView = (ImageView) findViewById(R.id.lightBulbImageView);
        mSpeechRecognizerManager =new SpeechRecognizerManager(this);
        mSpeechRecognizerManager.setOnResultListner(this);
    }
    private void hideLightBulb(){
        lightBulbImageView.setVisibility(View.INVISIBLE);
    }

    private void showLightBulb(){
        lightBulbImageView.setVisibility(View.VISIBLE);
    }

    private void turnYellow(){
        lightBulbImageView.setImageResource(R.drawable.yellow);
        lightBulbImageView.setVisibility(View.VISIBLE);
    }

    private void turnBlue(){
        lightBulbImageView.setImageResource(R.drawable.blue);
        lightBulbImageView.setVisibility(View.VISIBLE);
    }

    private void turnPink(){
        lightBulbImageView.setImageResource(R.drawable.pink);
        lightBulbImageView.setVisibility(View.VISIBLE);
    }

    @Override
    public void OnResult(ArrayList<String> commands) {

        for(String command:commands)
        {
            if (command.equals("hide")){
                Toast.makeText(this,"You said:"+command, Toast.LENGTH_SHORT).show();
                hideLightBulb();
                return;
            }

            if (command.equals("show")){
                Toast.makeText(this,"You said:"+command, Toast.LENGTH_SHORT).show();
                showLightBulb();
                return;
            }

            if (command.equals("turn yellow")){
                Toast.makeText(this,"You said:"+command, Toast.LENGTH_SHORT).show();
                turnYellow();
                return;
            }

            if (command.equals("turn blue")){
                Toast.makeText(this,"You said:"+command, Toast.LENGTH_SHORT).show();
                turnBlue();
                return;
            }

            if (command.equals("turn pink")){
                Toast.makeText(this,"You said:"+command, Toast.LENGTH_SHORT).show();
                turnPink();
                return;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

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

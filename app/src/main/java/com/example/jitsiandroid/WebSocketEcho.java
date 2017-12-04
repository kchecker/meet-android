package com.example.jitsiandroid;

import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by kasumi on 11/21/17.
 */

public class WebSocketEcho implements Subject {
    private static WebSocketEcho INSTANCE = null;
    private Socket mSocket;
    //coordinates from togetherjs
    private double startX ,startY;

    //array list for observers
    private List<Observer> observers = new ArrayList<Observer>();

    public static WebSocketEcho getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new WebSocketEcho();
        }
        return INSTANCE;
    }

    public void run() throws IOException {
        try {
            mSocket = IO.socket("http://192.168.8.110:3030");
        } catch (URISyntaxException e) {}
        mSocket.connect();
        String username = "mobile";
        String room = "test";
        JSONObject user = new JSONObject();
        try {
            user.put("room", room);
            user.put("username", username);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("joinroom", user);
        Log.d("connect","connected");
        mSocket.on("update-position", onNewClick);
    }


    //add on click========================================================================================
    private Emitter.Listener onNewClick = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.d("EventEmit: ", "update-position");
            JSONObject data = (JSONObject) args[0];
            try {
                double X = (double)data.getInt("X")/(double)data.getInt("width");
                double Y = (double)data.getInt("Y")/(double)data.getInt("height");
                Log.d("ClickXX: ", String.valueOf(data.getInt("X")));
                Log.d("ClickYY: ", String.valueOf(data.getInt("Y")));
                setCoordinates(X,Y);
                Log.d("ClickUser: ", String.valueOf(data));
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    };

    //observer design pattern added----------------------------------------------------------------------------------------
    public void registerObserver(Observer coordinatesObserver) {
        if(!observers.contains(coordinatesObserver)) {
            observers.add(coordinatesObserver);
        }
    }

    public void removeObserver(Observer coordinatesObserver) {
        if(observers.contains(coordinatesObserver)) {
            observers.remove(coordinatesObserver);
        }
    }

    public void notifyObservers() {
        for (Observer observer: observers) {
            observer.onCoordinatesChanged(startX ,startY);
        }
    }

    public void setCoordinates(double startX, double startY) {
        this.startX = startX;
        this.startY = startY;
        notifyObservers();
    }
}
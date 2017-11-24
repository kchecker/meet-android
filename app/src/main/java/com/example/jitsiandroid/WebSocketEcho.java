package com.example.jitsiandroid;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.ws.WebSocket;
import okhttp3.ws.WebSocketCall;
import okhttp3.ws.WebSocketListener;
import okio.Buffer;

import static okhttp3.ws.WebSocket.TEXT;

/**
 * Created by kasumi on 11/21/17.
 */

public class WebSocketEcho implements WebSocketListener,Subject {
    private static WebSocketEcho INSTANCE = null;
    //coordinates from togetherjs
    private int startX ,startY, endX, endY;

    private final Executor writeExecutor = Executors.newSingleThreadExecutor();
    //array list for observers
    private List<Observer> observers = new ArrayList<Observer>();

    public static WebSocketEcho getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new WebSocketEcho();
        }
        return INSTANCE;
    }

    public void run() throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("wss://hub.togetherjs.com/12345")
                .build();
        WebSocketCall.create(client, request).enqueue(this);

        // Trigger shutdown of the dispatcher's executor so this process can exit cleanly.
        client.dispatcher().executorService().shutdown();
    }

    @Override public void onOpen(final WebSocket webSocket, Response response) {
        writeExecutor.execute(new Runnable() {
            @Override public void run() {
                try {
                    webSocket.sendMessage(RequestBody.create(TEXT, "Hello..."));
                    webSocket.sendMessage(RequestBody.create(TEXT, "...World!"));
                } catch (IOException e) {
                    System.err.println("Unable to send messages: " + e.getMessage());
                }
            }
        });
    }

    @Override public void onMessage(ResponseBody message) throws IOException {
        if (message.contentType() == TEXT) {
            String togetherjs = message.string(); //get togetherjs 'msg' object
            Log.d("TOGETHERJS: " , togetherjs);
            try {
                //make a JSONObject and retrieve the required values
                JSONObject togetherjsObject = new JSONObject(togetherjs);
                String togetherjsType = togetherjsObject.getString("type");
                getCoordinates(togetherjsType, togetherjsObject); //to get coordinates of a drawing
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("MESSAGE: " + message.source().readByteString().hex());
        }
        //message.close(); //avoid continuous listening
    }

    //get coordinates------------------------------------------------
    public void getCoordinates(String togetherjsType, JSONObject togetherjsObject) throws JSONException {
        int firstCoordinateX, firstCoordinateY, secondCoordinateX, secondCoordinateY;
        switch (togetherjsType) {
            case "app.draw":
                firstCoordinateX = (int)Double.parseDouble(togetherjsObject.getJSONObject("start").getString("x"));
                firstCoordinateY = (int)Double.parseDouble(togetherjsObject.getJSONObject("start").getString("y"));
                secondCoordinateX = (int)Double.parseDouble(togetherjsObject.getJSONObject("end").getString("x"));
                secondCoordinateY = (int)Double.parseDouble(togetherjsObject.getJSONObject("end").getString("y"));
                //setCoordinates(firstCoordinateX, firstCoordinateY, secondCoordinateX, secondCoordinateY);
                break;
            case "cursor-click":
                firstCoordinateX = (int)Double.parseDouble(togetherjsObject.getString("offsetX"));
                firstCoordinateY = (int)Double.parseDouble(togetherjsObject.getString("offsetY"));
                setCoordinates(firstCoordinateX,firstCoordinateY,0,0);
                break;
            default:
                //setCoordinates(0,0,0,0);
                break;
        }
    }

    @Override public void onPong(Buffer payload) {
        System.out.println("PONG: " + payload.readUtf8());
    }

    @Override public void onClose(int code, String reason) {
        System.out.println("CLOSE: " + code + " " + reason);
    }

    @Override public void onFailure(IOException e, Response response) {
        e.printStackTrace();
    }

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
            observer.onCoordinatesChanged(startX ,startY, endX, endY);
        }
    }

    public void setCoordinates(int startX, int startY, int endX, int endY) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        notifyObservers();
    }
}
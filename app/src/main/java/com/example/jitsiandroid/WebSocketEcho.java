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
    private int startX ,startY, endX, endY;

    private final Executor writeExecutor = Executors.newSingleThreadExecutor();
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
                    /*webSocket.sendMessage(RequestBody.create(BINARY, ByteString.decodeHex("deadbeef")));
                    webSocket.close(1000, "Goodbye, World!");*/
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
                JSONObject obj = new JSONObject(togetherjs);
                String type = obj.getString("type");
                getCoordinates(type, obj); //to get coordinates of a drawing
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("MESSAGE: " + message.source().readByteString().hex());
        }
        //message.close(); //avoid continuous listening
    }

    //get coordinates------------------------------------------------
    public void getCoordinates(String type, JSONObject obj) throws JSONException {
        int firstCoordinateX, firstCoordinateY, secondCoordinateX, secondCoordinateY;
        switch (type) {
            case "app.draw":
                firstCoordinateX = Integer.parseInt(obj.getJSONObject("start").getString("x"));
                firstCoordinateY = Integer.parseInt(obj.getJSONObject("start").getString("y"));
                secondCoordinateX = Integer.parseInt(obj.getJSONObject("end").getString("x"));
                secondCoordinateY = Integer.parseInt(obj.getJSONObject("end").getString("y"));
                Log.d("APP.DRAW: ", obj.getJSONObject("start").getString("x"));
                Log.d("APP.DRAW: ", obj.getJSONObject("start").getString("y"));
                Log.d("APP.DRAW: ", obj.getJSONObject("end").getString("x"));
                Log.d("APP.DRAW: ", obj.getJSONObject("end").getString("y"));
                setCoordinates(firstCoordinateX, firstCoordinateY, secondCoordinateX, secondCoordinateY);
                Log.d("ON-MOVE: ","fffffffffdr");
                break;
            case "cursor-click":
                firstCoordinateX = Integer.parseInt(obj.getString("offsetX"));
                firstCoordinateY = Integer.parseInt(obj.getString("offsetY"));
                setCoordinates(firstCoordinateX,firstCoordinateY,0,0);
                Log.d("CURSOR_CLICK: ", obj.getString("offsetX"));
                Log.d("CURSOR_CLICK: ", obj.getString("offsetY"));
                Log.d("ON-MOVE: ","fffffffffcli");
                break;
            default:
                setCoordinates(0,0,0,0);
                Log.d("NO DRAW: ", type);
                Log.d("ON-MOVE: ","fffffffff787");
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
            Log.d("ON-MOVE: ","fffffffffk");
        }
    }

    public void setCoordinates(int startX, int startY, int endX, int endY) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        notifyObservers();
        Log.d("ON-MOVE: ","fffffffff0f");
    }
}
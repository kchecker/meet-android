package com.example.jitsiandroid;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;
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

public class WebSocketEcho implements WebSocketListener {

    private final Executor writeExecutor = Executors.newSingleThreadExecutor();

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
        switch (type) {
            case "init-connection":
                Log.d("INIT_CONNECTION: ", obj.getString("peer-count"));
                break;
            case "app.draw":
                setStartX(Integer.parseInt(obj.getJSONObject("start").getString("x")));
                setStartY(Integer.parseInt(obj.getJSONObject("start").getString("y")));
                setEndX(Integer.parseInt(obj.getJSONObject("end").getString("x")));
                setEndY(Integer.parseInt(obj.getJSONObject("end").getString("y")));
                Log.d("APP.DRAW: ", obj.getJSONObject("start").getString("x"));
                Log.d("APP.DRAW: ", obj.getJSONObject("start").getString("y"));
                Log.d("APP.DRAW: ", obj.getJSONObject("end").getString("x"));
                Log.d("APP.DRAW: ", obj.getJSONObject("end").getString("y"));
                break;
            case "cursor-click":
                Log.d("CURSOR_CLICK: ", obj.getString("offsetX"));
                Log.d("CURSOR_CLICK: ", obj.getString("offsetY"));
                break;
            default:
                Log.d("NO DRAW: ", type);
                break;
        }
    }
    private int setStartX(int x) {
        return x;
    }
    private int setStartY(int y) {
        return y;
    }
    private int setEndX(int x) {
        return x;
    }
    private int setEndY(int y) {
        return y;
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
}
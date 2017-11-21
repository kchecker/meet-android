package com.example.jitsiandroid;

import java.io.IOException;
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
            System.out.println("MESSAGE: " + message.string());
        } else {
            System.out.println("MESSAGE: " + message.source().readByteString().hex());
        }
        message.close();
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